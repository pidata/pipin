package io.pipin.core.settings

import java.util

import io.pipin.core.ext.EntitySink

/**
  * Created by libin on 2020/1/9.
  */
case class MergeSettings (keyMap:util.Map[String, util.List[String]], entitySink:EntitySink)
