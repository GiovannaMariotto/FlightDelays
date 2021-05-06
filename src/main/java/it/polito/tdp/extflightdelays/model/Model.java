package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	//grafo semplice,non orientato, pesato
	private SimpleWeightedGraph<Airport,DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer,Airport> idMap;
	private Map<Airport,Airport> visita;
	public Model() {
		dao= new ExtFlightDelaysDAO();
		idMap = new HashMap<Integer,Airport>();
		this.dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) {
		grafo = new SimpleWeightedGraph(DefaultWeightedEdge.class);
		//Aggiungo vertici "filtrati"
		Graphs.addAllVertices(grafo, dao.getVertici(x, idMap));
		
		//aggiungo gli archi
		for(Rotta r : dao.getRotte(idMap)) {
			//Se la rotta ha i due aeroporti: vedo se c'è già questo arco
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {
			DefaultWeightedEdge e =this.grafo.getEdge(r.getA1(), r.getA2());
			//getEdge() --> prende sia il edge(a1,a2) sia il edge(a2,a1)
			if(e==null) {
				Graphs.addEdgeWithVertices(grafo, r.getA1(), r.getA2());
			}else {
				double pesoVecchio = this.grafo.getEdgeWeight(e);
				double pesoNuovo = pesoVecchio + r.getN();
				this.grafo.setEdgeWeight(e, pesoNuovo); //Settare il peso
			} //setEdgeWeight() richiede un double
			}
		}
		System.out.println("Grafo creato!");
		System.out.println("#Vertici:"+grafo.vertexSet().size());
		System.out.println("#Archi:"+grafo.edgeSet().size());
		
	}

	public Set<Airport> getVertici() {
		
		return grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2){
		List<Airport> percorso = new LinkedList();
		BreadthFirstIterator<Airport,DefaultWeightedEdge> it = new BreadthFirstIterator<>(grafo,a1); 
		
		visita = new HashMap<>();
		visita.put(a1, null); //devo inserire  la radice
		it.addTraversalListener(new TraversalListener<Airport,DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				//Callback
				Airport airport1 = grafo.getEdgeSource(e.getEdge());
				Airport airport2 = grafo.getEdgeTarget(e.getEdge());
				
				if(visita.containsKey(airport1) && !visita.containsKey(airport2)) {
					visita.put(airport2, airport1);
				} else if (visita.containsKey(airport2) && !visita.containsKey(airport1)) {
					visita.put(airport1, airport2);
				}
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		while(it.hasNext()) {
			it.next();
			//Registra gli eventi (traversalListener)	
		}
		
		percorso.add(a2);//Aggiungo la destinazione
		Airport step=a2;
		//chi è il suo padre?
		while(visita.get(step)!=null) {
			step=visita.get(step);
			percorso.add(step);
		}
		
		return percorso;
	}//percorso ao contrario
	
	
	
	
}
