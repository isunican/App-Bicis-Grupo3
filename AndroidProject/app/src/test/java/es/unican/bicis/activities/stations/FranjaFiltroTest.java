package es.unican.bicis.activities.stations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.annotation.Config;

import es.unican.bicis.R;



/** Pruebas unitarias de la franja del filtro activo (botón "Quitar filtros"). */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class FranjaFiltroTest {

    private FranjaFiltro sut;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        sut = new FranjaFiltro(context);
    }

    @Test
    public void testMostrarHaceVisibleLaFranjaConElTexto() {
        sut.mostrar("Con al menos 3 bicis libres");

        TextView texto = sut.findViewById(R.id.tvFiltroActivo);
        assertEquals(View.VISIBLE, sut.getVisibility());
        assertEquals("Con al menos 3 bicis libres", texto.getText().toString());
    }

    @Test
    public void testOcultarEscondeLaFranja() {
        sut.mostrar("Con al menos 3 bicis libres");

        sut.ocultar();

        assertEquals(View.GONE, sut.getVisibility());
    }

    @Test
    public void testElBotonTieneElTextoQuitarFiltros() {
        Button boton = sut.findViewById(R.id.btnQuitarFiltros);

        assertEquals("Quitar filtros", boton.getText().toString());
    }

    @Test
    public void testPulsarElBotonAvisaAlListener() {
        AtomicInteger pulsaciones = new AtomicInteger(0);
        sut.setOnQuitarFiltrosListener(pulsaciones::incrementAndGet);

        sut.findViewById(R.id.btnQuitarFiltros).performClick();

        assertEquals(1, pulsaciones.get());
    }

    @Test
    public void testSinListenerPulsarElBotonNoFalla() {
        sut.setOnQuitarFiltrosListener(null);

        assertFalse(sut.findViewById(R.id.btnQuitarFiltros).performClick());
    }

    @Test
    public void testMostrarDosVecesActualizaElTexto() {
        sut.mostrar("Con al menos 2 bicis libres");
        sut.mostrar("Con al menos 5 bicis libres");

        TextView texto = sut.findViewById(R.id.tvFiltroActivo);
        assertEquals("Con al menos 5 bicis libres", texto.getText().toString());
        assertEquals(View.VISIBLE, sut.getVisibility());
    }
}