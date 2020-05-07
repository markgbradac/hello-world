/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.phy2000.kafka.connect.ultramessaging;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Very simple connector that works with the console. This connector supports both source and
 * sink modes via its 'mode' setting.
 */
public class UMSinkConnector extends SinkConnector {
    public static final String UM_CONFIG_FILE = "um.config.file";
    public static final String UM_LICENSE_FILE = "um.license.file";
    public static final String UM_TOPIC_PREFIX = "um.topic.prefix";
    public static final String FILE_CONFIG = "file";

    public static final String DEFAULT_UM_CONFIG_FILE = "/home/centos/um.config.file";
    public static final String DEFAULT_UM_LICENSE_FILE = "C:/Users/mbradac/Desktop/um-kafka-connect-master/um.license.file";
    public static final String DEFAULT_UM_TOPIC_PREFIX = "";
    public static final String DEFAULT_FILE_DOT_OUT = "file.out";

    private static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(UM_CONFIG_FILE, Type.STRING, DEFAULT_UM_CONFIG_FILE, Importance.HIGH,"Full path to the UM configuration file")
        .define(UM_LICENSE_FILE, Type.STRING, DEFAULT_UM_LICENSE_FILE, Importance.HIGH,"Full path to the UM License file")
        .define(UM_TOPIC_PREFIX, Type.STRING, DEFAULT_UM_TOPIC_PREFIX, Importance.HIGH,"UM source Topic prefix")
        .define(FILE_CONFIG, Type.STRING, DEFAULT_FILE_DOT_OUT, Importance.HIGH, "Destination filename. If not specified, the standard output will be used");

    private final UMSinkConnector.um_kafka_config um_config = new um_kafka_config();

    static class um_kafka_config {
        String um_config_file;
        String um_license_file;
        String um_topic_prefix;
        String filename;
    }

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
        um_config.um_config_file = parsedConfig.getString(UM_CONFIG_FILE);
        um_config.um_license_file = parsedConfig.getString(UM_LICENSE_FILE);
        um_config.um_topic_prefix = parsedConfig.getString(UM_TOPIC_PREFIX);
        um_config.filename = parsedConfig.getString(FILE_CONFIG);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return UMSinkTask.class;
    }

    // creates a configuration for each task
    // each task is a separate thread assigned to one or more partitions
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> taskConfigs = new ArrayList<>();
        for (int i = 0; i < maxTasks; i++) {
            Map<String, String> config = new HashMap<>();
            config.put(UM_CONFIG_FILE, um_config.um_config_file);
            config.put(UM_LICENSE_FILE, um_config.um_license_file);
            config.put(UM_TOPIC_PREFIX, um_config.um_topic_prefix);
            if (um_config.filename != null)
                config.put(FILE_CONFIG, um_config.filename);
            taskConfigs.add(config);
        }
        return taskConfigs;
    }

    @Override
    public void stop() {
        // Nothing to do since UMSinkConnector has no background monitoring.
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }
}
