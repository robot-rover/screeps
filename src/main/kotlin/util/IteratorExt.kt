package util

fun <T> Iterator<T>.nextOrNull(): T? = if (this.hasNext()) { this.next() } else { null }