

package cn.doraro.flexedge.ext.msg_net;

import cn.doraro.flexedge.core.msgnet.IMNRunner;
import cn.doraro.flexedge.core.msgnet.MNModule;
import cn.doraro.flexedge.core.msgnet.MNNode;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;

import java.time.Duration;
import java.util.*;

public class Kafka_M extends MNModule implements IMNRunner {
    static ILogger log;

    static {
        Kafka_M.log = LoggerManager.getLogger((Class) Kafka_M.class);
    }

    String brokerHost;
    int brokerPort;
    long sendTo;
    String user;
    String psw;
    int producerAck;
    int producerRetries;
    SecurityProto securityProto;
    SaslMech saslMech;
    Thread RT_th;
    boolean RT_bRun;
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    private transient HashMap<String, KafkaIn_NS> topic2in;
    private Runnable consumerRunner;

    public Kafka_M() {
        this.brokerPort = 9092;
        this.sendTo = 1000L;
        this.user = "";
        this.psw = "";
        this.producerAck = 1;
        this.producerRetries = 3;
        this.securityProto = SecurityProto.PLAINTEXT;
        this.saslMech = SaslMech.PLAIN;
        this.RT_th = null;
        this.RT_bRun = false;
        this.producer = null;
        this.consumer = null;
        this.topic2in = null;
        this.consumerRunner = new Runnable() {
            @Override
            public void run() {
                Kafka_M.this.consumerRun();
            }
        };
    }

    public String getTP() {
        return "kafka";
    }

    public String getTPTitle() {
        return "Kafka";
    }

    public String getColor() {
        return "#debed7";
    }

    public String getIcon() {
        return "\\uf0ec";
    }

    public boolean isParamReady(final StringBuilder failedr) {
        if (Convert.isNullOrEmpty(this.brokerHost)) {
            failedr.append("no host set");
            return false;
        }
        if (this.brokerPort <= 0) {
            failedr.append("invalid port");
            return false;
        }
        return true;
    }

    public JSONObject getParamJO() {
        final JSONObject jo = new JSONObject();
        jo.putOpt("host", (Object) this.brokerHost);
        jo.putOpt("port", (Object) ((this.brokerPort > 0) ? this.brokerPort : 9092));
        jo.putOpt("sec_proto", (Object) this.securityProto.id);
        jo.putOpt("sec_sasl_mech", (Object) this.saslMech.id);
        jo.putOpt("send_to", (Object) this.sendTo);
        jo.putOpt("user", (Object) this.user);
        jo.putOpt("psw", (Object) this.psw);
        return jo;
    }

    protected void setParamJO(final JSONObject jo) {
        this.brokerHost = jo.optString("host", "");
        this.brokerPort = jo.optInt("port", 9092);
        this.securityProto = SecurityProto.forId((short) jo.optInt("sec_proto", 0));
        this.saslMech = SaslMech.fromId((short) jo.optInt("sec_sasl_mech", 0));
        this.sendTo = jo.optLong("send_to", 1000L);
        this.user = jo.optString("user", "");
        this.psw = jo.optString("psw", "");
    }

    private boolean RT_init(final List<String> recv_topics, final StringBuilder failedr) {
        if (Convert.isNullOrEmpty(this.brokerHost) || this.brokerPort <= 0) {
            failedr.append("no borker host port set");
            return false;
        }
        try {
            final Properties properties = new Properties();
            properties.put("bootstrap.servers", String.valueOf(this.brokerHost) + ":" + this.brokerPort);
            properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            properties.put("acks", "1");
            properties.put("retries", "5");
            Label_0305:
            {
                switch (this.securityProto) {
                    case SASL_PLAINTEXT: {
                        properties.put("security.protocol", "SASL_PLAINTEXT");
                        properties.put("sasl.mechanism", this.saslMech.name);
                        switch (this.saslMech) {
                            case PLAIN: {
                                properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + this.user + "\" password=\"" + this.psw + "\";");
                                break Label_0305;
                            }
                            case SCRAM_SHA_256:
                            case SCRAM_SHA_512: {
                                properties.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + this.user + "\" password=\"" + this.psw + "\";");
                                break Label_0305;
                            }
                        }
                        break;
                    }
                }
            }
            this.producer = (KafkaProducer<String, String>) new KafkaProducer(properties);
            if (recv_topics.size() > 0) {
                properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                properties.put("group.id", "experiment");
                (this.consumer = (KafkaConsumer<String, String>) new KafkaConsumer(properties)).subscribe((Collection) recv_topics);
            }
        } catch (final Exception ee) {
            ee.printStackTrace();
            failedr.append(ee.getMessage());
            return false;
        }
        return true;
    }

    public synchronized boolean RT_start(final StringBuilder failedr) {
        if (this.RT_bRun) {
            return true;
        }
        final List<MNNode> rns = this.getRelatedNodes();
        if (rns == null || rns.size() <= 0) {
            failedr.append("no related nodes found");
            return false;
        }
        final HashMap<String, KafkaIn_NS> topic2in = new HashMap<String, KafkaIn_NS>();
        final ArrayList<String> recvtopics = new ArrayList<String>();
        final ArrayList<String> sendtopics = new ArrayList<String>();
        for (final MNNode rn : rns) {
            if (rn instanceof KafkaIn_NS) {
                final KafkaIn_NS kin = (KafkaIn_NS) rn;
                final String topic = kin.getTopic();
                if (Convert.isNullOrEmpty(topic)) {
                    continue;
                }
                topic2in.put(topic, kin);
                recvtopics.add(topic);
            } else {
                if (!(rn instanceof KafkaOut_NE)) {
                    continue;
                }
                final KafkaOut_NE kout = (KafkaOut_NE) rn;
                final String topic = kout.getTopic();
                if (Convert.isNullOrEmpty(topic)) {
                    continue;
                }
                sendtopics.add(topic);
            }
        }
        if (topic2in.size() <= 0 && sendtopics.size() <= 0) {
            failedr.append("no related Kafka In or Out topic found");
            return false;
        }
        this.topic2in = topic2in;
        if (!this.RT_init(recvtopics, failedr)) {
            return false;
        }
        this.RT_bRun = true;
        (this.RT_th = new Thread(this.consumerRunner)).start();
        return true;
    }

    public synchronized void RT_stop() {
        this.RT_bRun = false;
    }

    public boolean RT_isRunning() {
        return this.RT_th != null;
    }

    public boolean RT_isSuspendedInRun(final StringBuilder reson) {
        return false;
    }

    public boolean RT_runnerEnabled() {
        return true;
    }

    public boolean RT_runnerStartInner() {
        return false;
    }

    void RT_send(final String topic, final String msg) {
        if (this.producer == null) {
            this.RT_DEBUG_WARN.fire("send", "not send msg : [" + topic + "] size=" + msg.length() + ",may be Module is not running.");
            return;
        }
        final long st = System.currentTimeMillis();
        try {
            this.RT_DEBUG_INF.fire("send", "before send msg : [" + topic + "] size=" + msg.length(), msg);
            final ProducerRecord<String, String> record = (ProducerRecord<String, String>) new ProducerRecord(topic, (Object) msg);
            this.producer.send((ProducerRecord) record);
            this.RT_DEBUG_INF.fire("send", "send msg : [" + topic + "] size=" + msg.length() + " cost=" + (System.currentTimeMillis() - st) + "MS", msg);
        } catch (final Exception ee) {
            ee.printStackTrace();
            this.RT_DEBUG_ERR.fire("send", "send msg : [" + topic + "] size=" + msg.length(), (Throwable) ee);
        }
    }

    private void consumerRun() {
        try {
            if (this.consumer == null) {
                return;
            }
            while (this.RT_bRun) {
                try {
                    final ConsumerRecords<String, String> records = (ConsumerRecords<String, String>) this.consumer.poll(Duration.ofMillis(1000L));
                    for (final ConsumerRecord<String, String> record : records) {
                        final String topic = record.topic();
                        final String msg = (String) record.value();
                        final KafkaIn_NS kin = this.topic2in.get(topic);
                        if (kin != null) {
                            kin.RT_onTopicMsgRecv(topic, msg);
                        }
                    }
                } catch (final Throwable ee) {
                    if (Kafka_M.log.isDebugEnabled()) {
                        Kafka_M.log.debug("consumer error", ee);
                    }
                    try {
                        Thread.sleep(100L);
                    } catch (final Exception ex) {
                    }
                }
            }
        } finally {
            Label_0292:
            {
                if (this.producer != null) {
                    try {
                        this.producer.close();
                    } catch (final Exception e) {
                        e.printStackTrace();
                        this.producer = null;
                        break Label_0292;
                    } finally {
                        this.producer = null;
                    }
                    this.producer = null;
                }
            }
            Label_0339:
            {
                if (this.consumer != null) {
                    try {
                        this.consumer.close();
                    } catch (final Exception e) {
                        e.printStackTrace();
                        this.consumer = null;
                        break Label_0339;
                    } finally {
                        this.consumer = null;
                    }
                    this.consumer = null;
                }
            }
            this.RT_bRun = false;
            this.RT_th = null;
        }
        Label_0399:
        {
            if (this.producer != null) {
                try {
                    this.producer.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                    break Label_0399;
                } finally {
                    this.producer = null;
                }
                this.producer = null;
            }
        }
        Label_0446:
        {
            if (this.consumer != null) {
                try {
                    this.consumer.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                    break Label_0446;
                } finally {
                    this.consumer = null;
                }
                this.consumer = null;
            }
        }
        this.RT_bRun = false;
        this.RT_th = null;
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
}
