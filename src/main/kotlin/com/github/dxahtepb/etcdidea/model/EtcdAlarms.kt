package com.github.dxahtepb.etcdidea.model

data class EtcdAlarms(val alarms: List<EtcdAlarmMember>)

data class EtcdAlarmMember(val memberId: Long, val alarmType: EtcdAlarmType)

enum class EtcdAlarmType {
    NOSPACE, // space quota is exhausted
    UNRECOGNIZED
}
