package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.MathUtils
import com.esotericsoftware.minlog.Log
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.*

class BulletUpdateAndRenderSystem(val gameplay : GameplayScreen) : EntitySystem() {
    lateinit var entities: ImmutableArray<Entity>

    private val bulletMapper = ComponentMapper.getFor(BulletComponent::class.java)

    override fun addedToEngine(engine: Engine?) {
        if (engine == null)
            return
        entities = engine.getEntitiesFor(
                Family.all(
                        BulletComponent::class.java
                ).get()
        )
    }

    override fun update(deltaTime: Float) {
        for (entity in entities) {
            val bullet = bulletMapper.get(entity)

            bullet.lifetime -= deltaTime
            if(bullet.lifetime <= 0f) {
                bullet.dispose()
                engine.removeEntity(entity)
                continue
            }

            val aff = Affine2()
            aff.translate(bullet.body.position)
            aff.rotate(bullet.body.angle * MathUtils.radiansToDegrees)
            val textureRegion = TextureRegion(bullet.texture)

            gameplay.worldSpriteBatch.draw(textureRegion, bullet.texture.width.toFloat(), bullet.texture.height.toFloat(), aff)
        }
    }

}