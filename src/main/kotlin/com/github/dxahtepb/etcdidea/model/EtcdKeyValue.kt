package com.github.dxahtepb.etcdidea.model

import io.etcd.jetcd.KeyValue
import java.nio.charset.StandardCharsets

data class EtcdKeyValue(val key: String, val value: String) {
    companion object Factory {
        fun fromKeyValue(kv: KeyValue): EtcdKeyValue {
            return EtcdKeyValue(kv.getKeyAsString(), kv.getValueAsString())
        }
    }
}

internal fun KeyValue.getKeyAsString() = this.key.toString(StandardCharsets.UTF_8)
internal fun KeyValue.getValueAsString() = this.value.toString(StandardCharsets.UTF_8)
