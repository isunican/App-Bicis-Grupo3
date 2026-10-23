package es.unican.bicis.activities.stations;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import es.unican.bicis.R;

/**
 * Franja que se muestra en la pantalla de estaciones mientras hay un filtro aplicado.
 * Indica qué filtro está activo y tiene el botón "Quitar filtros".
 * Es solo un componente visual: no tiene lógica, avisa de la pulsación al listener
 * y es el presenter quien decide qué hacer.
 */
public class FranjaFiltro extends LinearLayout {

    private final TextView tvFiltroActivo;
    private final Button btnQuitarFiltros;

    public FranjaFiltro(Context context) {
        this(context, null);
    }

    public FranjaFiltro(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.view_franja_filtro, this, true);
        tvFiltroActivo = findViewById(R.id.tvFiltroActivo);
        btnQuitarFiltros = findViewById(R.id.btnQuitarFiltros);
    }

    /**
     * Muestra la franja con el texto del filtro activo.
     * @param textoFiltro texto que describe el filtro aplicado
     */
    public void mostrar(String textoFiltro) {
        tvFiltroActivo.setText(textoFiltro);
        setVisibility(View.VISIBLE);
    }

    /**
     * Oculta la franja (cuando no hay filtros aplicados).
     */
    public void ocultar() {
        setVisibility(View.GONE);
    }

    /**
     * Indica qué hacer cuando el usuario pulsa "Quitar filtros".
     * @param listener acción a ejecutar (puede ser null)
     */
    public void setOnQuitarFiltrosListener(Runnable listener) {
        if (listener == null) {
            btnQuitarFiltros.setOnClickListener(null);
        } else {
            btnQuitarFiltros.setOnClickListener(v -> listener.run());
        }
    }
}