package util

interface SetDirty {
    fun setDirty()
}

interface SetParent {
    fun setParent(newParent: SetDirty)
}

class Root<out T>(children: Array<SetParent>, private val calc: () -> T): SetDirty {
    init {
        children.forEach { it.setParent(this) }
    }

    private var value: T = calc()
    private var isDirty: Boolean = true

    override fun setDirty() {
        val newValue = calc()
        val different = newValue != value
        value = newValue
        if (different) {
            isDirty = true
        }
    }

    fun checkDirty(): Boolean {
        val wasDirty = isDirty
        isDirty = false
        return wasDirty
    }


}

class Chain<out T>(children: Array<SetParent> = arrayOf(), private val calc: () -> T): SetDirty, SetParent {
    init {
        children.forEach { it.setParent(this) }
    }
    private var parent: SetDirty? = null

    private var value: T = calc()

    override fun setDirty() {
        val newValue = calc()
        val different = newValue != value
        value = newValue
        if (different) {
            parent?.setDirty()
        }
    }

    fun get(): T {
        return value
    }

    override fun setParent(newParent: SetDirty) {
        if (parent != null) {
            logWarn("Overwriting parent on ${this::class}")
        }
        parent = newParent
    }
}

class Leaf<T>(initValue: T): SetDirty, SetParent {
    private var parent: SetDirty? = null
    private var value: T = initValue


    fun get(): T {
        return value
    }

    fun set(newValue: T) {
        val different = newValue != value
        value = newValue
        if (different) {
            parent?.setDirty()
        }
    }

    override fun setDirty() {
        parent?.setDirty()
    }

    override fun setParent(newParent: SetDirty) {
        if (parent != null) {
            logWarn("Overwriting parent on ${this::class}")
        }
        parent = newParent
    }
}