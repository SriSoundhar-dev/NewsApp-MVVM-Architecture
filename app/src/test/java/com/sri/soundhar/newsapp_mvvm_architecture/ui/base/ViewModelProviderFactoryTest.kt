package com.sri.soundhar.newsapp_mvvm_architecture.ui.base

import androidx.lifecycle.ViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class ViewModelProviderFactoryTest {

    @Test
    fun `create returns the instance the creator produced`() {
        val expected = FakeViewModel()
        val factory = ViewModelProviderFactory(FakeViewModel::class) { expected }

        val created = factory.create(FakeViewModel::class.java)

        assertSame(expected, created)
    }

    @Test
    fun `create rejects an unrelated ViewModel class`() {
        val factory = ViewModelProviderFactory(FakeViewModel::class) { FakeViewModel() }

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            factory.create(OtherViewModel::class.java)
        }

        assertEquals("Unknown class name", thrown.message)
    }

    @Test
    fun `create runs the creator on every call rather than caching`() {
        var built = 0
        val factory = ViewModelProviderFactory(FakeViewModel::class) {
            built++
            FakeViewModel()
        }

        val first = factory.create(FakeViewModel::class.java)
        val second = factory.create(FakeViewModel::class.java)

        // Caching is ViewModelProvider's job, not this factory's — which is why
        // ActivityModule hands the factory to a ViewModelProvider instead of calling it.
        assertEquals(2, built)
        assertSame(first, first)
        assert(first !== second)
    }

    @Test
    fun `create also accepts a supertype of the class it was built for`() {
        val expected = FakeViewModel()
        val factory = ViewModelProviderFactory(FakeViewModel::class) { expected }

        // The guard is `modelClass.isAssignableFrom(kClass.java)`, i.e. "is modelClass a
        // supertype of the declared class", so asking for a plain ViewModel succeeds too.
        // Harmless as the app uses it today, but it means the factory is looser than the
        // "Unknown class name" message suggests.
        val created = factory.create(ViewModel::class.java)

        assertSame(expected, created)
    }
}

private class FakeViewModel : ViewModel()

private class OtherViewModel : ViewModel()
