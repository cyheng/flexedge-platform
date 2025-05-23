Node: Tags Event Trigger
==



In many cases, we obtain the required real-time Tag data through a timer and then perform subsequent processing. But
this scenario cannot meet the following scenario: changes in some on-site signals represent a specific event that must
be recorded every time. If a timer is used for sampling, there is a high possibility of data loss.
The changes can be captured by defining tag events to address this situation.

This node configures by selecting events from the tags, listens for events generated by changes of tags during runtime,
and generates event messages to trigger outputs based on certain strategies.

### Parameter settings

Double click to open the node parameter settings dialog box

#### Out Mode

When listening event is triggered or released, the node can have three message output modes:

1. Trigger output: Output a message when an event is triggered;

2. Release output: Output a message when the generated event is released;

3. Both trigger and release outputs: When an event is triggered and released, there will be message outputs; At this
   point, the node will have two output points. The first is the output when triggered, and the second is the output
   message when released;

#### Tag Events

By selecting the corresponding events definition under the tag, this node can listen to and process the triggering and
release of events.

### Output payload format

1. Trigger message format

```
"payload": {
		"trigger_dt": 1717996146986,
		"triggered": true,
		"evt_tp": "val_gt",
		"evt_prompt": "water high",
		"evt_id": "00LX8IJQY200010",
		"tag_id": "r17",
		"evt_tpt": "Val >",
		"tag_path": "ch1.aio.wl_val",
		"tag_val": 4.049375
	}
```

2. Release message format

```
"payload": {
    "trigger_dt": 1717996146986,
    "evt_tp": "val_gt",
    "evt_prompt": "water high",
    "evt_id": "00LX8IJQY200010",
    "tag_id": "r17",
    "release_dt": 1717996174791,
    "evt_tpt": "Val >",
    "tag_path": "ch1.aio.wl_val",
    "tag_val": 3.6294272,
    "released": true
	}
```
