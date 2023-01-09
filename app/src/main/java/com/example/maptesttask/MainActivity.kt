package com.example.maptesttask

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.layers.TransitionOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mapView: MapView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.DARK) { style ->
            style.transition = TransitionOptions(0, 0, false)
            initLayerIcons(style)
            addClusteredGeoJsonSource(style)
        }
    }

    private fun initLayerIcons(loadedMapStyle: Style) {
        BitmapUtils.getBitmapFromDrawable(
            ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_wifi)
        )?.let {
            loadedMapStyle.addImage(
                CLUSTER, it
            )
        }
    }


    private fun addClusteredGeoJsonSource(loadedMapStyle: Style) {
        try {
            loadedMapStyle.addSource(
                GeoJsonSource(
                    MARKERS,
                    URI("asset://points.geojson"),
                    GeoJsonOptions()
                        .withCluster(true)
                        .withClusterMaxZoom(25)
                        .withClusterRadius(15)
                )
            )
        } catch (uriSyntaxException: URISyntaxException) {
            Timber.e("Check the URL %s", uriSyntaxException.message)
        }

        val unclusteredSymbolLayer =
            SymbolLayer("unclustered-points", MARKERS).withProperties(
                iconImage(CLUSTER),
                iconSize(0.8f)
            )

        loadedMapStyle.addLayer(unclusteredSymbolLayer)

        val layers = intArrayOf(0, 100, 1000, 10000)

        for (i in layers.indices) {
            val symbolLayer = SymbolLayer("cluster-$i", MARKERS)
            symbolLayer.setProperties(
                iconImage(CLUSTER),
                iconSize(1.0f + i)
            )
            val pointCount = toNumber(get(POINT_COUNT))

            symbolLayer.setFilter(
                if (i == 3) all(
                    has(POINT_COUNT),
                    gte(pointCount, literal(layers[i]))
                ) else all(
                    has(POINT_COUNT),
                    gte(pointCount, literal(layers[i])),
                    lt(pointCount, literal(layers[i + 1]))
                )
            )
            loadedMapStyle.addLayer(symbolLayer)
        }

    }

    public override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    companion object {
        private const val CLUSTER = "cluster"
        private const val MARKERS = "markers"
        private const val POINT_COUNT = "point_count"
    }
}
