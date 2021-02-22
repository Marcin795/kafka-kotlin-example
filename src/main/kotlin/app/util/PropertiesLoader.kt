package app.util

import java.util.*

class PropertiesLoader {

    companion object {

        fun loadProperties(): Properties {
            val s = javaClass.classLoader.getResourceAsStream("application.properties")
            val properties = Properties()
            properties.load(s)
            return properties
        }

    }

}
