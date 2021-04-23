package phraseapp.internal

import phraseapp.internal.platforms.Android
import phraseapp.internal.platforms.Flutter
import phraseapp.internal.platforms.iOS

enum class Platform {
    ANDROID {
        override fun toNewPlatform(): phraseapp.internal.platforms.Platform = Android
    },
    IOS {
        override fun toNewPlatform(): phraseapp.internal.platforms.Platform = iOS
    },
    FLUTTER {
        override fun toNewPlatform(): phraseapp.internal.platforms.Platform = Flutter
    };

    abstract fun toNewPlatform(): phraseapp.internal.platforms.Platform
}
