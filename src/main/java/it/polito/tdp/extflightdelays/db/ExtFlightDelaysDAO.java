package it.polito.tdp.extflightdelays.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.extflightdelays.model.Airline;
import it.polito.tdp.extflightdelays.model.Airport;
import it.polito.tdp.extflightdelays.model.Flight;
import it.polito.tdp.extflightdelays.model.Rotta;

public class ExtFlightDelaysDAO {

	public List<Airline> loadAllAirlines() {
		String sql = "SELECT * from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new Airline(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRLINE")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public void loadAllAirports(Map<Integer,Airport> idMap) {
		String sql = "SELECT * FROM airports";
		

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				if(!idMap.containsKey(rs.getInt("ID"))) {
					Airport airport = new Airport(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRPORT"),
						rs.getString("CITY"), rs.getString("STATE"), rs.getString("COUNTRY"), rs.getDouble("LATITUDE"),
						rs.getDouble("LONGITUDE"), rs.getDouble("TIMEZONE_OFFSET"));
					idMap.put(airport.getId(), airport);
				}
				
				
			}

			conn.close();
			

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Flight> loadAllFlights() {
		String sql = "SELECT * FROM flights";
		List<Flight> result = new LinkedList<Flight>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Flight flight = new Flight(rs.getInt("ID"), rs.getInt("AIRLINE_ID"), rs.getInt("FLIGHT_NUMBER"),
						rs.getString("TAIL_NUMBER"), rs.getInt("ORIGIN_AIRPORT_ID"),
						rs.getInt("DESTINATION_AIRPORT_ID"),
						rs.getTimestamp("SCHEDULED_DEPARTURE_DATE").toLocalDateTime(), rs.getDouble("DEPARTURE_DELAY"),
						rs.getDouble("ELAPSED_TIME"), rs.getInt("DISTANCE"),
						rs.getTimestamp("ARRIVAL_DATE").toLocalDateTime(), rs.getDouble("ARRIVAL_DELAY"));
				result.add(flight);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Airport> getVertici(int x,Map<Integer,Airport> idMap){
		String sql="SELECT  a.id as 'id_vertice' "
				+ "FROM airports a,flights f "
				+ "WHERE (a.ID=f.ORIGIN_AIRPORT_ID OR a.id = f.DESTINATION_AIRPORT_ID )  "
				+ "GROUP BY a.id "
				+ "HAVING COUNT(DISTINCT(f.AIRLINE_ID))>?";
		List<Airport> result = new LinkedList();
		
		try {
			
		Connection conn = ConnectDB.getConnection();
		PreparedStatement st = conn.prepareStatement(sql);
		st.setInt(1, x);
		ResultSet rs = st.executeQuery();
		
		while(rs.next()) {
			result.add(idMap.get(rs.getInt("id_vertice")));
			
		}
		rs.close();
		st.close();
		conn.close();
		return result;
		
		
		}catch(SQLException e) {
			e.printStackTrace();
			System.out.println("Error Connection Database");
			return null;
		}
		
	}

	public List<Rotta> getRotte(Map<Integer,Airport> idMap){
		String sql="SELECT  f.ORIGIN_AIRPORT_ID as id_a1, f.DESTINATION_AIRPORT_ID as id_a2, COUNT(*) AS 'n' "
				+ "FROM flights f "
				+ "GROUP BY f.ORIGIN_AIRPORT_ID, f.DESTINATION_AIRPORT_ID ";
		List<Rotta> result = new LinkedList<Rotta>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				Airport sorgente = idMap.get(rs.getInt("id_a1"));
				Airport destinazione = idMap.get(rs.getInt("id_a2"));
				if(sorgente!=null && destinazione!=null) {
					result.add(new Rotta(idMap.get(rs.getInt("id_a1")),
						idMap.get(rs.getInt("id_a2")),rs.getInt("n")));
				}else {
					System.out.println("Errore nel metodo getRota() nel DB");
				}
				
			}
			rs.close();
				st.close();
				conn.close();
				return result;
			
		}catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Error: methodo getRotte del DB");

		}
		
		
		
		
	}

}
