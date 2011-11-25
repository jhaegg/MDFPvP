package se.shard.kaustic.mdfpvp;

import java.util.ArrayList;
import java.util.PriorityQueue;
import org.bukkit.Chunk;

/**
 * Class for performing search between chunks in claims.
 * @author Johan Hägg
 */
public class ClaimSearch {	
	public enum SearchResult {
		Found,
		NotDone,
		NotFound
	};
	
	/**
	 * Storage class for search state.
	 * @author Johan Hägg
	 */
	private class SearchState implements Comparable<SearchState> {
		private Chunk chunk;
		private double actual;
		private double heuristic;
		
		public SearchState(Chunk chunk, double actual, double heuristic) {
			this.chunk = chunk;
			this.actual = actual;
			this.heuristic = heuristic;
		}

		@Override
		public int compareTo(SearchState o) {						
			return (int)Math.signum((this.actual + this.heuristic) - (o.actual + this.heuristic)); 
		}
		
		public double getActual() {
			return this.actual;
		}
	}
	
	private Chunk end;
	private ArrayList<Chunk> visited;
	private PriorityQueue<SearchState> search;
	private DatabaseView view;
	
	/**
	 * Calculates the straight line distance between two chunks.
	 * @param a The first chunk.
	 * @param b The second chunk.
	 * @return The straight line distance between the provided chunks.
	 */
	private double chunkDistance(Chunk a, Chunk b) {
		return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getZ() - b.getZ(), 2)); 
	}
	
	/**
	 * Starts a new search between two claimed chunks.
	 * @param start The starting chunk for the search.
	 * @param end The target chunk.
	 * @param view The database view containing claim information.
	 */
	public ClaimSearch(Chunk start, Chunk end, DatabaseView view) {
		this.end = end;
		this.view = view;
		visited = new ArrayList<Chunk>();
		search = new PriorityQueue<SearchState>();
		search.add(new SearchState(start, 0, chunkDistance(start, end)));
		visited.add(start); 
	}

	/**
	 * Starts a new search between two claimed chunks.
	 * @param start The starting chunk for the search.
	 * @param end The target chunk.
	 * @param ignore Chunk which should be ignored.
	 * @param view The database view containing claim information.
	 */
	public ClaimSearch(Chunk start, Chunk end, Chunk ignore, DatabaseView view) {
		this.end = end;
		this.view = view;
		visited = new ArrayList<Chunk>();
		search = new PriorityQueue<SearchState>();
		search.add(new SearchState(start, 0, chunkDistance(start, end)));
		visited.add(start);
		visited.add(ignore);
	}
	
	/**
	 * Evaluate the next search step.
	 * @return The result of the search.
	 */
	private SearchResult step() {
		SearchState state = search.poll();
		
		if(state == null) {
			return SearchResult.NotFound;
		}
		
		for(Chunk chunk : view.getNeighboringClaimedChunks(state.chunk)) {
			if(!visited.contains(chunk)) {
				if(chunk == end) {
					return SearchResult.Found;
				}
				
				visited.add(chunk);
				search.add(new SearchState(chunk, state.getActual() + 1, chunkDistance(chunk, end)));
			}
		}
		
		return SearchResult.NotDone;
	}
	
	/**
	 * Checks if the end chunk is reachable from the current chunk.
	 * @return True if reachable, otherwise false.
	 */
	public boolean isReachable() {
		SearchResult r;
		
		do {
			r = step();
		} while(r == SearchResult.NotDone);
	
		return r == SearchResult.Found;
	}
}
