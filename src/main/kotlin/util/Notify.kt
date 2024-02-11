package util

interface SetDirty {
    fun setDirty()
}

class Root<out T>(private val calc: () -> T): SetDirty {
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

class Chain<out T>(private val parent: SetDirty? = null, private val calc: () -> T): SetDirty {
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
}

class Leaf<T>(private val parent: SetDirty?, initValue: T): SetDirty {
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
}