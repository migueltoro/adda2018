package us.lsi.ag.anuncios;


import java.util.List;

import us.lsi.ag.SeqNomalChromosome;
import us.lsi.ag.SeqNormalProblemAG;
import us.lsi.anuncios.datos.Anuncio;
import us.lsi.anuncios.datos.ListaDeAnunciosAEmitir;
import us.lsi.anuncios.datos.DatosAnuncios;

public class ProblemaAnunciosAG extends DatosAnuncios implements SeqNormalProblemAG<ListaDeAnunciosAEmitir> {

	public ProblemaAnunciosAG(String file) {
		super();
		super.leeYOrdenaAnuncios(file);
	}	
	
	@Override
	public ListaDeAnunciosAEmitir getSolucion(SeqNomalChromosome c) {		
		return  ListaDeAnunciosAEmitir.create(c.decode());
	}
	

	@Override
	public Double fitnessFunction(SeqNomalChromosome c) {	
		List<Integer> list = c.decode();
		ListaDeAnunciosAEmitir ls = ListaDeAnunciosAEmitir.create(list);
		Double valor = ls.getValor();
		Integer nIncompatibles = ls.getNumAnunciosIncompatibles();
		Integer tr = ls.getTiempoRestante()>=0?0:-ls.getTiempoRestante();
		Integer nRepetidos = ls.getNumAnunciosRepetidos();
		Integer d = nIncompatibles*nIncompatibles+nRepetidos*nRepetidos+tr*tr;
		Double f = valor/1000000L  - 1000000000000L*d*d;
		return f;
	}

	
	public List<Anuncio> getObjetos() {
		return DatosAnuncios.todosLosAnunciosDisponibles;
	}

	@Override
	public Integer getObjectsNumber() {
		return this.getObjetos().size();
	}
	
	
}
