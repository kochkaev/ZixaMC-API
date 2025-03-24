package ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync

class TBAssertionFailed(val msg: String) : Exception("$msg Please report this to https://github.com/vanutp/tgbridge/issues")