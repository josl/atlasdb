package com.palantir.atlasdb.keyvalue.partition;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedBytes;
import com.palantir.atlasdb.keyvalue.partition.endpoint.SimpleKeyValueEndpoint;
import com.palantir.atlasdb.keyvalue.partition.quorum.QuorumParameters;
import com.palantir.atlasdb.spi.AtlasDbFactory;
import com.palantir.timestamp.InMemoryTimestampService;
import com.palantir.timestamp.TimestampService;

public class PartitionedAtlasDbFactory implements AtlasDbFactory<PartitionedKeyValueService> {

    @Override
    public String getType() {
        return "partitioned";
    }

    @Override
    public PartitionedKeyValueService createRawKeyValueService(JsonNode config)
            throws IOException {
        return PartitionedKeyValueService.create(createConfig(config.get("partitionedConfig")));
    }

    @Override
    public TimestampService createTimestampService(
            PartitionedKeyValueService rawKvs) {
        return new InMemoryTimestampService();
    }

    private PartitionedKeyValueConfiguration createConfig(JsonNode node) {
        int repf = node.get("replicationFactor").asInt();
        int readf = node.get("readFactor").asInt();
        int writef = node.get("writeFactor").asInt();
        QuorumParameters parameters = new QuorumParameters(repf, readf, writef);

        Map<byte[], SimpleKeyValueEndpoint> endpoints = Maps.newTreeMap(UnsignedBytes.lexicographicalComparator());
        Iterator<JsonNode> endpointsIterator = node.get("endpoints").elements();
        while (endpointsIterator.hasNext()) {
            JsonNode endpointNode = endpointsIterator.next();
            String kvsUri = endpointNode.get("kvsUri").asText();
            String pmsUri = endpointNode.get("pmsUri").asText();
            byte[] key = BaseEncoding.base16().decode(endpointNode.get("key").asText());
            endpoints.put(key, new SimpleKeyValueEndpoint(kvsUri, pmsUri));
        }

        return new PartitionedKeyValueConfiguration(parameters, endpoints);
    }

}