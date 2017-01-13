package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.util.Dynamic

val FilterTrim = Filter("trim") { subject, _ -> Dynamic.toString(subject).trim() }