package ru.pamparam.pw.clientcore

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.attachments.*
import com.badlogic.gdx.physics.box2d.PolygonShape


class Box2dAttachment(name : String?) : RegionAttachment(name)
{
    var body : Body? = null
}

class PwHeroAnimation {
    val skeleton : Skeleton
    val animationState : AnimationState
    constructor(atlasFilename : String, skelFilename : String) {
        val atlas = TextureAtlas(atlasFilename)

        val atlasLoader = ru.pamparam.pw.clientcore.MyAttachmentLoader(atlas)

        val skeletonBinary = SkeletonJson(atlasLoader)
        skeletonBinary.scale = 0.3f

        val skeletonData = skeletonBinary.readSkeletonData(Gdx.files.internal(skelFilename))
        val animationData = AnimationStateData(skeletonData)
        animationState = AnimationState(animationData)
        skeleton = Skeleton(skeletonData)

        for (slot in skeleton.slots) {
            if (slot.attachment !is ru.pamparam.pw.clientcore.Box2dAttachment) continue
            val attachment = slot.attachment as ru.pamparam.pw.clientcore.Box2dAttachment

            //val boxPoly = PolygonShape()
            //boxPoly.setAsBox(attachment.width / 2 * attachment.scaleX,
            //        attachment.height / 2 * attachment.scaleY, Vector2(attachment.x, attachment.y),
            //        attachment.lookRotation * MathUtils.degRad)

            //val boxBodyDef = BodyDef()
            //boxBodyDef.type = BodyDef.BodyType.StaticBody
            //attachment.body = PamparamWorld.myWorld.physicWorld.createBody(boxBodyDef)
            //attachment.body!!.createFixture(boxPoly, 1f)

            //boxPoly.dispose()
        }
    }

    fun render(position : Vector2, rotation : Float, timeDelta : Float, batch: Batch, skeletonRenderer: SkeletonRenderer) {
        animationState.update(timeDelta)
        animationState.apply(skeleton)

        skeleton.rootBone.rotation = rotation
        skeleton.rootBone.setPosition(position.x, position.y)

        for (slot in skeleton.slots) {
            if (slot.attachment !is ru.pamparam.pw.clientcore.Box2dAttachment) continue
            val attachment = slot.attachment as ru.pamparam.pw.clientcore.Box2dAttachment
            if (attachment.body != null) {
                attachment.body?.setTransform(
                        slot.bone.worldX,
                        slot.bone.worldY,
                        rotation * MathUtils.degRad)
            }
        }

        skeleton.updateWorldTransform()

        skeletonRenderer.draw(batch, skeleton)
        //val debug = SkeletonRendererDebug()
        //debug.draw(skeleton)
    }
}