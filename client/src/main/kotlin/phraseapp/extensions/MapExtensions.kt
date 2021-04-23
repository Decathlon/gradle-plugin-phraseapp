package phraseapp.extensions

fun <K, V> Iterable<Pair<K, V>>.associateWithList(): Map<K, List<V>> {
    val map = mutableMapOf<K, MutableList<V>>()
    forEach {
        if (map.containsKey(it.first)) {
            val list = map[it.first]!!
            list.add(it.second)
            map.replace(it.first, list)
        } else {
            map[it.first] = arrayListOf(it.second)
        }
    }
    return map
}