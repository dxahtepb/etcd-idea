package com.github.dxahtepb.etcdidea

import io.etcd.jetcd.ByteSequence
import java.nio.charset.StandardCharsets

internal fun String.toByteSequence() = ByteSequence.from(this, StandardCharsets.UTF_8)
