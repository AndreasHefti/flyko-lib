package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.core.api.DesktopAppAdapter

fun main() {
    DesktopApp("IntroTest", 800, 600) {
        it.dispose()
        DesktopAppAdapter.exit()
    }
}