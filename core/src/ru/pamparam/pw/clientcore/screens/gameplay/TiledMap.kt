package ru.pamparam.pw.clientcore.screens.gameplay

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.esotericsoftware.minlog.Log
import kotlin.math.floor
import com.badlogic.gdx.maps.objects.*
import com.badlogic.gdx.physics.box2d.*


class TiledMap(val gameplayScreen: GameplayScreen, val batch : SpriteBatch) {
    val img by lazy { Texture("warcraft_summer.png") }
    val tiledMap by lazy { TmxMapLoader().load("test_map.tmx") }
    val tiledMapRenderer by lazy { OrthogonalTiledMapRenderer(tiledMap, batch) }

    val objectBodied = mutableListOf<Body>()
    val ppt = 1f
    init {

        val objects = tiledMap.layers.get("impassability_objects")?.objects
        if(objects != null) {
            val borderNames = arrayOf("border_top", "border_bottom", "border_left", "border_right")
            val borderBodyDef = BodyDef()
            borderBodyDef.type = BodyDef.BodyType.StaticBody;
            val body = gameplayScreen.box2dWorld.createBody(borderBodyDef)
            body.position.x = 0f
            body.position.y = 0f
            for(border in borderNames) {
                val recObject = objects.get(border)
                Log.info("Makebody1 ${border} ${recObject}")
                if(recObject is RectangleMapObject) {
                    Log.info("Makebody2 ${border}")
                    val borderFixtureDef = FixtureDef()
                    borderFixtureDef.shape = getRectangle(recObject)
                    body.createFixture(borderFixtureDef);
                }
            }
            objectBodied.add(body);
        }

        tiledMap.layers.get("impassability_objects")?.objects?.forEach {
            val shape = when(it) {
                //is RectangleMapObject -> getRectangle(it)
                //is PolygonMapObject -> getPolygon(it)
                //is PolylineMapObject -> getPolyline(it)
                is EllipseMapObject -> getCircle(it)
                else -> null
            }
            Log.info("${it.name} ${it}")
            if(shape != null) {

                val bodyDef = BodyDef()
                bodyDef.type = BodyDef.BodyType.StaticBody;
                val body = gameplayScreen.box2dWorld.createBody(bodyDef)
                body.createFixture(shape, 1f);
                objectBodied.add(body);
                shape.dispose();
            }
        }
    }

    fun draw(camera: OrthographicCamera) {
        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()
    }

    private fun getRectangle(rectangleObject: RectangleMapObject): PolygonShape {
        val rectangle = rectangleObject.rectangle
        val polygon = PolygonShape()
        val size = Vector2((rectangle.x + rectangle.width * 0.5f) / ppt,
                (rectangle.y + rectangle.height * 0.5f) / ppt)
        polygon.setAsBox(rectangle.width * 0.5f / ppt,
                rectangle.height * 0.5f / ppt,
                size,
                0.0f)
        return polygon
    }

    private fun getCircle(ellipseObject: EllipseMapObject): CircleShape {
        val ellipse = ellipseObject.getEllipse()
        val circleShape = CircleShape()
        circleShape.radius = ellipse.width * 0.5f/ ppt
        circleShape.position = Vector2(ellipse.x + ellipse.width * 0.5f/ ppt, ellipse.y + ellipse.width * 0.5f/ ppt)
        return circleShape
    }

    private fun getPolygon(polygonObject: PolygonMapObject): PolygonShape {
        val polygon = PolygonShape()
        val vertices = polygonObject.getPolygon().getTransformedVertices()

        val worldVertices = FloatArray(vertices.size)

        for (i in vertices.indices) {
            println(vertices[i])
            worldVertices[i] = vertices[i] / ppt
        }

        polygon.set(worldVertices)
        return polygon
    }

    private fun getPolyline(polylineObject: PolylineMapObject): ChainShape {
        val vertices = polylineObject.getPolyline().getTransformedVertices()
        val worldVertices = arrayOfNulls<Vector2>(vertices.size / 2)

        for (i in 0 until vertices.size / 2) {
            val point = Vector2()
            point.x = vertices[i * 2] / ppt
            point.y = vertices[i * 2 + 1] / ppt
            worldVertices[i] = point
        }

        val chain = ChainShape()
        chain.createChain(worldVertices)
        return chain
    }
}