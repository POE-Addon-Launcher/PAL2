package PAL2.GUI

import javafx.scene.effect.Light
import javafx.scene.effect.Lighting
import javafx.scene.paint.Color

/**
 *
 */
object LightningEffects
{
    fun noUpdateLighting(): Lighting
    {
        val l = Lighting()
        l.diffuseConstant = 2.0
        l.specularConstant = 0.0
        l.specularExponent = 0.0
        l.surfaceScale = 0.0
        l.light = Light.Distant()
        l.light.color = Color.color(0.0/255.0, 119.0/255.0, 42.0/255.0)
        return l
    }

    fun updateAvailLighting(): Lighting
    {
        val l = Lighting()
        l.diffuseConstant = 2.0
        l.specularConstant = 0.0
        l.specularExponent = 0.0
        l.surfaceScale = 0.0
        l.light = Light.Distant()
        l.light.color = Color.color(141.0/255.0, 100.0/255.0, 47.0/255.0)
        return l
    }
}