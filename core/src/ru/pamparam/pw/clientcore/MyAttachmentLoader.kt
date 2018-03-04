package ru.pamparam.pw.clientcore

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.esotericsoftware.spine.Skin
import com.esotericsoftware.spine.attachments.*

class MyAttachmentLoader(val atlas : TextureAtlas) : AttachmentLoader {
    override fun newBoundingBoxAttachment(skin: Skin?, name: String?): BoundingBoxAttachment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun newMeshAttachment(skin: Skin?, name: String?, path: String?): MeshAttachment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun newClippingAttachment(skin: Skin?, name: String?): ClippingAttachment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun newPathAttachment(skin: Skin?, name: String?): PathAttachment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun newPointAttachment(skin: Skin?, name: String?): PointAttachment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun newRegionAttachment(skin: Skin?, name: String?, path: String?): RegionAttachment {
        val attachment = ru.pamparam.pw.clientcore.Box2dAttachment(name)
        val region = atlas.findRegion(attachment.name) ?: throw RuntimeException("Region not found in atlas: " + attachment)
        attachment.region = region
        return attachment
    }
}