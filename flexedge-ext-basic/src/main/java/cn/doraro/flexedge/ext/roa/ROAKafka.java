// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.ext.roa;

import cn.doraro.flexedge.core.router.*;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ROAKafka extends RouterOuterAdp {
    static ILogger log;

    static {
        ROAKafka.log = LoggerManager.getLogger((Class) ROAKafka.class);
    }

    String brokerHost;
    int brokerPort;
    long sendTo;
    String user;
    String psw;
    int producerAck;
    int producerRetries;
    ArrayList<SendConf> sendConfs;
    ArrayList<RecvConf> recvConfs;
    SecurityProto securityProto;
    SaslMech saslMech;
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    private Thread th;
    private ArrayList<JoinIn> leftJoinIns;
    private ArrayList<JoinOut> leftJoinOuts;
    private boolean bRTInitOk;
    private Runnable consumerRunner;

    public ROAKafka(final RouterManager rm) {
        super(rm);
        this.brokerPort = 9092;
        this.sendTo = 1000L;
        this.user = "";
        this.psw = "";
        this.producerAck = 1;
        this.producerRetries = 3;
        this.sendConfs = new ArrayList<SendConf>();
        this.recvConfs = new ArrayList<RecvConf>();
        this.securityProto = SecurityProto.PLAINTEXT;
        this.saslMech = SaslMech.PLAIN;
        this.th = null;
        this.leftJoinIns = null;
        this.leftJoinOuts = null;
        this.bRTInitOk = false;
        this.consumerRunner = new Runnable() {
            @Override
            public void run() {
                ROAKafka.this.consumerRun();
            }
        };
    }

    public String getTp() {
        return "kafka";
    }

    public RouterOuterAdp newInstance(final RouterManager rm) {
        return new ROAKafka(rm);
    }

    public String getBrokerHost() {
        if (this.brokerHost == null) {
            return "";
        }
        return this.brokerHost;
    }

    public int getBrokerPort() {
        return this.brokerPort;
    }

    public long getSendTimeout() {
        return this.sendTo;
    }

    public SecurityProto getSecurityProto() {
        return this.securityProto;
    }

    public SaslMech getSaslMech() {
        return this.saslMech;
    }

    public String getUser() {
        if (this.user == null) {
            return "";
        }
        return this.user;
    }

    public String getPsw() {
        if (this.psw == null) {
            return "";
        }
        return this.psw;
    }

    public ROAKafka asBroker(final String host, final int port) {
        this.brokerHost = host;
        this.brokerPort = port;
        return this;
    }

    public ROAKafka asBrokerAuth(final String user, final String psw) {
        this.user = user;
        this.psw = psw;
        return this;
    }

    public ROAKafka asProducerPM(final int ack, final int retries) {
        this.producerAck = ack;
        this.producerRetries = retries;
        return this;
    }

    public List<SendConf> getSendConfs() {
        return this.sendConfs;
    }

    public SendConf getSendConfByName(final String name) {
        if (this.sendConfs == null) {
            return null;
        }
        for (final SendConf sc : this.sendConfs) {
            if (sc.name.equals(name)) {
                return sc;
            }
        }
        return null;
    }

    public List<JoinIn> getJoinInList() {
        if (this.leftJoinIns != null) {
            return this.leftJoinIns;
        }
        synchronized (this) {
            final ArrayList<JoinIn> jis = new ArrayList<JoinIn>();
            if (this.sendConfs != null) {
                for (final SendConf sc : this.sendConfs) {
                    final JoinIn ji = new JoinIn((RouterNode) this, sc.name);
                    ji.setTitleDesc(sc.getShowTitle(), "");
                    ji.setRelatedObj((Object) sc);
                    jis.add(ji);
                }
            }
            this.leftJoinIns = jis;
        }
        return this.leftJoinIns;
    }

    public List<JoinOut> getJoinOutList() {
        if (this.leftJoinOuts != null) {
            return this.leftJoinOuts;
        }
        synchronized (this) {
            final ArrayList<JoinOut> jis = new ArrayList<JoinOut>();
            if (this.recvConfs != null) {
                for (final RecvConf sc : this.recvConfs) {
                    final JoinOut jo = new JoinOut((RouterNode) this, sc.name);
                    jo.setTitleDesc(sc.getShowTitle(), "");
                    jo.setRelatedObj((Object) sc);
                    jis.add(jo);
                }
            }
            this.leftJoinOuts = jis;
        }
        return this.leftJoinOuts;
    }

    protected void RT_onRecvedFromJoinIn(final JoinIn ji, final RouterObj recved_data) throws Exception {
        if (!this.bRTInitOk) {
            this.RT_fireErr("ROAKafka is not init ok", (Throwable) null);
            return;
        }
        final String jin_n = ji.getName();
        final SendConf sc = this.getSendConfByName(jin_n);
        if (sc == null) {
            return;
        }
        final String topic = sc.topic;
        final String txt = recved_data.getTxt();
        if (txt == null) {
            return;
        }
        final ProducerRecord<String, String> pr = (ProducerRecord<String, String>) new ProducerRecord(topic, (Object) txt);
        this.send(pr);
    }

    public JSONObject toJO() {
        final JSONObject jo = super.toJO();
        jo.put("host", (Object) this.brokerHost);
        jo.put("port", this.brokerPort);
        jo.put("send_to", this.sendTo);
        jo.put("user", (Object) this.user);
        jo.put("psw", (Object) this.psw);
        jo.put("ack", this.producerAck);
        jo.put("retries", this.producerRetries);
        jo.put("sec_proto", (int) this.securityProto.id);
        jo.put("sec_sasl_mech", this.saslMech.id);
        JSONArray jar = new JSONArray();
        for (final SendConf sc : this.sendConfs) {
            jar.put((Object) sc.toJO());
        }
        jo.put("send_confs", (Object) jar);
        jar = new JSONArray();
        for (final RecvConf rc : this.recvConfs) {
            jar.put((Object) rc.toJO());
        }
        jo.put("recv_confs", (Object) jar);
        return jo;
    }

    protected boolean fromJO(final JSONObject jo, final StringBuilder failedr) {
        if (!super.fromJO(jo, failedr)) {
            return false;
        }
        this.brokerHost = jo.optString("host", "");
        this.brokerPort = jo.optInt("port", 9092);
        this.sendTo = jo.optLong("send_to", 1000L);
        this.user = jo.optString("user", "");
        this.psw = jo.optString("psw", "");
        this.producerAck = jo.optInt("ack", 1);
        this.producerRetries = jo.optInt("retries", 3);
        final short t = (short) jo.optInt("sec_proto", 0);
        this.securityProto = SecurityProto.forId(t);
        this.saslMech = SaslMech.fromId(jo.optInt("sec_sasl_mech", 0));
        JSONArray jarr = jo.optJSONArray("send_confs");
        final ArrayList<SendConf> scs = new ArrayList<SendConf>();
        if (jarr != null) {
            for (int n = jarr.length(), i = 0; i < n; ++i) {
                final JSONObject tmpjo = jarr.getJSONObject(i);
                final SendConf sc = new SendConf();
                if (!sc.fromJO(tmpjo, failedr)) {
                    return false;
                }
                scs.add(sc);
            }
            this.sendConfs = scs;
        }
        jarr = jo.optJSONArray("recv_confs");
        final ArrayList<RecvConf> rcs = new ArrayList<RecvConf>();
        if (jarr != null) {
            for (int n2 = jarr.length(), j = 0; j < n2; ++j) {
                final JSONObject tmpjo2 = jarr.getJSONObject(j);
                final RecvConf sc2 = new RecvConf();
                if (!sc2.fromJO(tmpjo2, failedr)) {
                    return false;
                }
                rcs.add(sc2);
            }
            this.recvConfs = rcs;
        }
        return true;
    }

    protected void RT_init() {
        this.bRTInitOk = false;
        if (Convert.isNullOrEmpty(this.brokerHost) || this.brokerPort <= 0) {
            throw new RuntimeException("no borker host port set");
        }
        try {
            final Properties properties = new Properties();
            properties.put("bootstrap.servers", String.valueOf(this.brokerHost) + ":" + this.brokerPort);
            properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            properties.put("acks", "1");
            properties.put("retries", "5");
            Label_0321:
            {
                switch (this.securityProto) {
                    case SASL_PLAINTEXT: {
                        properties.put("security.protocol", "SASL_PLAINTEXT");
                        properties.put("sasl.mechanism", this.saslMech.name);
                        switch (this.saslMech) {
                            case PLAIN: {
                                properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + this.user + "\" password=\"" + this.psw + "\";");
                                break Label_0321;
                            }
                            case SCRAM_SHA_256:
                            case SCRAM_SHA_512: {
                                properties.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + this.user + "\" password=\"" + this.psw + "\";");
                                break Label_0321;
                            }
                        }
                        break;
                    }
                }
            }
            this.producer = (KafkaProducer<String, String>) new KafkaProducer(properties);
            final ArrayList<String> recv_tps = new ArrayList<String>();
            if (this.recvConfs != null && this.recvConfs.size() > 0) {
                for (final RecvConf rc : this.recvConfs) {
                    final String tps = rc.topic;
                    if (Convert.isNullOrEmpty(tps)) {
                        continue;
                    }
                    recv_tps.add(tps);
                }
            }
            if (recv_tps.size() > 0) {
                properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                properties.put("group.id", "experiment");
                (this.consumer = (KafkaConsumer<String, String>) new KafkaConsumer(properties)).subscribe((Collection) recv_tps);
            }
            this.bRTInitOk = true;
            this.RT_fireErr((String) null, (Throwable) null);
        } catch (final Exception ee) {
            ee.printStackTrace();
            this.RT_fireErr(ee.getMessage(), (Throwable) ee);
        }
    }

    private void consumerRun() {
        if (this.consumer == null) {
            return;
        }
        Label_0197:
        {
            break Label_0197;
            try {
                while (true) {
                    try {
                        final ConsumerRecords<String, String> records = (ConsumerRecords<String, String>) this.consumer.poll(Duration.ofMillis(1000L));
                        for (final ConsumerRecord<String, String> record : records) {
                            final String topic = record.topic();
                            final String msg = (String) record.value();
                            final List<JoinOut> jos = this.getJoinOutList();
                            if (jos != null) {
                                if (jos.size() <= 0) {
                                    continue;
                                }
                                for (final JoinOut jo : jos) {
                                    final RecvConf rc = (RecvConf) jo.getRelatedObj();
                                    if (topic.equals(rc.topic)) {
                                        this.RT_sendToJoinOut(jo, new RouterObj((Object) msg));
                                    }
                                }
                            }
                        }
                    } catch (final Throwable ee) {
                        if (ROAKafka.log.isDebugEnabled()) {
                            ROAKafka.log.debug("consumer error", ee);
                        }
                        try {
                            Thread.sleep(100L);
                        } catch (final Exception ex) {
                        }
                    }
                    if (this.th == null) {
                        break;
                    }
                }
            } finally {
                this.th = null;
                if (this.producer != null) {
                    this.producer.close();
                    this.producer = null;
                }
                if (this.consumer != null) {
                    this.consumer.close();
                    this.consumer = null;
                }
            }
        }
        this.th = null;
        if (this.producer != null) {
            this.producer.close();
            this.producer = null;
        }
        if (this.consumer != null) {
            this.consumer.close();
            this.consumer = null;
        }
    }

    protected synchronized boolean RT_start_ov() {
        if (this.th != null) {
            return true;
        }
        this.RT_init();
        (this.th = new Thread(this.consumerRunner)).start();
        return true;
    }

    public synchronized void RT_stop() {
        final Thread tmpth = this.th;
        if (tmpth != null) {
            tmpth.interrupt();
        }
        if (this.producer != null) {
            this.producer.close();
            this.producer = null;
        }
        if (this.consumer != null) {
            this.consumer.close();
            this.consumer = null;
        }
        this.th = null;
    }

    public boolean RT_isRunning() {
        return this.th != null;
    }

    public void send(final ProducerRecord<String, String> record) throws InterruptedException, ExecutionException, TimeoutException {
        if (this.producer == null) {
            return;
        }
        this.producer.send((ProducerRecord) record).get(this.sendTo, TimeUnit.MILLISECONDS);
    }

    public void sendAsync(final ProducerRecord<String, String> record, final Callback callback) {
        if (this.producer == null) {
            return;
        }
        this.producer.send((ProducerRecord) record, callback);
    }

    public enum SaslMech {
        PLAIN("PLAIN", 0, 0, "PLAIN"),
        SCRAM_SHA_256("SCRAM_SHA_256", 1, 1, "SCRAM-SHA-256"),
        SCRAM_SHA_512("SCRAM_SHA_512", 2, 2, "SCRAM-SHA-512");

        public final int id;
        public final String name;

        private SaslMech(final String name2, final int ordinal, final int id, final String name) {
            this.id = (short) id;
            this.name = name;
        }

        public static SaslMech fromId(final int id) {
            switch (id) {
                case 0: {
                    return SaslMech.PLAIN;
                }
                case 1: {
                    return SaslMech.SCRAM_SHA_256;
                }
                case 2: {
                    return SaslMech.SCRAM_SHA_512;
                }
                default: {
                    return SaslMech.PLAIN;
                }
            }
        }
    }

    public enum SecurityProto {
        PLAINTEXT("PLAINTEXT", 0, 0, "PLAINTEXT"),
        SASL_PLAINTEXT("SASL_PLAINTEXT", 1, 2, "SASL_PLAINTEXT");

        private static final Map<Short, SecurityProto> CODE_TO_SECURITY_PROTOCOL;
        private static final List<String> NAMES;

        static {
            final SecurityProto[] protocols = values();
            final List<String> names = new ArrayList<String>(protocols.length);
            final Map<Short, SecurityProto> codeToSecurityProtocol = new HashMap<Short, SecurityProto>(protocols.length);
            SecurityProto[] array;
            for (int length = (array = protocols).length, i = 0; i < length; ++i) {
                final SecurityProto proto = array[i];
                codeToSecurityProtocol.put(proto.id, proto);
                names.add(proto.name);
            }
            CODE_TO_SECURITY_PROTOCOL = Collections.unmodifiableMap((Map<? extends Short, ? extends SecurityProto>) codeToSecurityProtocol);
            NAMES = Collections.unmodifiableList((List<? extends String>) names);
        }

        public final short id;
        public final String name;

        private SecurityProto(final String name2, final int ordinal, final int id, final String name) {
            this.id = (short) id;
            this.name = name;
        }

        public static List<String> names() {
            return SecurityProto.NAMES;
        }

        public static SecurityProto forId(final short id) {
            return SecurityProto.CODE_TO_SECURITY_PROTOCOL.get(id);
        }

        public static SecurityProto forName(final String name) {
            return valueOf(name.toUpperCase(Locale.ROOT));
        }
    }

    public static class SendConf {
        String id;
        String name;
        String topic;
        String title;
        String desc;

        public String getShowTitle() {
            if (Convert.isNotNullEmpty(this.title)) {
                return this.title;
            }
            return this.name;
        }

        public JSONObject toJO() {
            final JSONObject jo = new JSONObject();
            jo.put("id", (Object) this.id);
            jo.put("n", (Object) this.name);
            jo.put("topic", (Object) this.topic);
            jo.putOpt("t", (Object) this.title);
            jo.putOpt("d", (Object) this.desc);
            return jo;
        }

        public boolean fromJO(final JSONObject jo, final StringBuilder failedr) {
            this.id = jo.getString("id");
            this.name = jo.getString("n");
            this.topic = jo.getString("topic");
            this.title = jo.optString("t", "");
            this.desc = jo.optString("d", "");
            return true;
        }
    }

    public static class RecvConf {
        String id;
        String name;
        String topic;
        String title;
        String desc;

        public String getShowTitle() {
            if (Convert.isNotNullEmpty(this.title)) {
                return this.title;
            }
            return this.name;
        }

        public JSONObject toJO() {
            final JSONObject jo = new JSONObject();
            jo.put("id", (Object) this.id);
            jo.putOpt("n", (Object) this.name);
            jo.put("topic", (Object) this.topic);
            jo.putOpt("t", (Object) this.title);
            jo.putOpt("d", (Object) this.desc);
            return jo;
        }

        public boolean fromJO(final JSONObject jo, final StringBuilder failedr) {
            this.id = jo.getString("id");
            this.name = jo.optString("n");
            this.topic = jo.getString("topic");
            this.title = jo.optString("t", "");
            this.desc = jo.optString("d", "");
            return true;
        }
    }
}
