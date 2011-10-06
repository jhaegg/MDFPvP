package se.shard.kaustic.mdfpvp.services;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;

import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Service for exporting claim region overlays usable in brownan overviewer.
 * @author Johan Hägg
 */
public class ClaimRegionExporter implements Runnable {
	private MDFPvP plugin;
	private final HashMap<Chunk, Integer> heightMap = new HashMap<Chunk, Integer>();
	private String[] colors = {"#0000FF", "#00FF00", "#FF0000", "#FF00FF", "#FFFF00", "#00FFFF",						   
							   "#007FFF", "#7F00FF", "#7FFF00", "#00FF7F", "#FF7F00", "#FF007F",
							   "#7F7FFF", "#7FFF7F", "#FF7F7F", "#FF7FFF", "#FFFF7F", "#7FFFFF",
							   "#3F7FFF", "#7F3FFF", "#7FFF3F", "#3FFF7F", "#FF7F3F", "#FF3F7F",
							   "#000000", "#3F3F3F", "#808080", "#7F7F7F", "#C0C0C0", "#FFFFFF"};
	
	public ClaimRegionExporter(MDFPvP plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Get the y coordinate of the highest non-air block at the world x, z coordinates.
	 * @param world the world which should be used.
	 * @param x the world x coordinate
	 * @param z the world z coordinate
	 * @return the y coordinate of the highest non-air block at the give coordinates.
	 */
	private int getHighestYAt(World world, int x, int z) {
		Chunk chunk = world.getBlockAt(x, 0, z).getChunk();
		if(!chunk.isLoaded()) {
			chunk.load();
		}
		for(int y = 128; y > 0; y--) {
			if(world.getBlockTypeIdAt(x, y, z) != Material.AIR.getId()) {
				return y;
			}
		}
		return 0;
	}
	
	/**
	 * Finds the highest point on a chunk and stores it if needed.
	 * @param chunk the chunk for which the highest point should be found.
	 * @return the y coordinate of the highest point on the chunk.
	 */
	private int getHeightOfChunk(Chunk chunk) {
		if(heightMap.containsKey(chunk)) {
			return heightMap.get(chunk);
		}
		else {
			int max = 0;
			for(int z = 0; z < 16; z++) {
				for(int x = 0; x < 16; x++) {
					//int height = chunk.getWorld().getHighestBlockYAt(chunk.getX() * 16 + x, chunk.getZ() * 16 + z);
					int height = getHighestYAt(chunk.getWorld(), chunk.getX() * 16 + x, chunk.getZ() * 16 + z);
					if(height > max)
						max = height;
				}
			}
			heightMap.put(chunk, max);
			return max;
		}
	}
	
	/**
	 * Gets the average height at the top left corner of the given coordinates.
	 * @param world the world in which the corner is.
	 * @param x the x coordinate of the corner.
	 * @param z the z coordinate of the corner.
	 * @return the average height at the top left corner of the given coordinates.
	 */
	private int getAverageCornerHeight(World world, int x, int z) {
		int sum = 0;
		for(int pz = -1; pz < 1; pz++) {
			for(int px = -1; px < 1; px++) {
				sum += getHighestYAt(world, x + px, z + pz);
			}
		}
		return sum/4;
	}

	/**
	 * Exports a triangle in the brownan overviewer format.
	 * @param writer the file to which the triangle should be written.
	 * @param colorIndex the index of the color to use.
	 * @param x1 the x coordinate of the first triangle vertex.
	 * @param y1 the y coordinate of the first triangle vertex.
	 * @param z1 the z coordinate of the first triangle vertex.
	 * @param x2 the x coordinate of the second triangle vertex.
	 * @param y2 the y coordinate of the second triangle vertex.
	 * @param z2 the z coordinate of the second triangle vertex.
	 * @param x3 the x coordinate of the third triangle vertex.
	 * @param y3 the y coordinate of the third triangle vertex.
	 * @param z3 the z coordinate of the third triangle vertex.
	 * @throws IOException
	 */
	private void exportTriangle(FileWriter writer, int colorIndex, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) throws IOException {
		writer.write("  {\"color\": \"" + colors[colorIndex] + "\", \"opacity\": 0.5, \"closed\": true, \"path\": [\n");
		writer.write("    {\"x\": " + x1 + ", \"y\": "+ y1 + ", \"z\": " + z1 + "},\n" );
		writer.write("    {\"x\": " + x2 + ", \"y\": "+ y2 + ", \"z\": " + z2 + "},\n" );
		writer.write("    {\"x\": " + x3 + ", \"y\": "+ y3 + ", \"z\": " + z3 + "},\n" );
		writer.write("  ]},\n");
	}
	
	/**
	 * Exports the chunk overlay to brownan region.js file.
	 * @throws IOException
	 */
	private void exportRegionOverlay() throws IOException {
		FileWriter writer = null;
		int colorIndex = 0;

		try {
			writer = new FileWriter("regions.js");
			// Write header
			writer.write("overviewer.collections.regionDatas.push([\n");
			// Export all claims, changing color between players.
			for(UUID playerUUID : plugin.getDatabaseView().getPlayerUUIDs()) {
				for(Chunk chunk : plugin.getDatabaseView().getClaimedChunks(playerUUID)) {
					
					int mid = getHeightOfChunk(chunk);
					int x = chunk.getX() * 16;
					int z = chunk.getZ() * 16;
					// pattern used for walking between points.
					int pattern[] = {0, 0, 8, 16, 16, 16, 8, 0};
					int px = 0;
					int pz = 0;
					int py = getAverageCornerHeight(chunk.getWorld(), x + px, z + pz);
					int cx, cy, cz;
					// Iterate over the pattern and export a square composed of 8 triangles.
					for(int index = 0; index < 8; index++) {
						 cx = pattern[(index + 2) % pattern.length];
						 cz = pattern[index];
						 cy = getAverageCornerHeight(chunk.getWorld(), x + cx, z + cz);
						 exportTriangle(writer, colorIndex, x + 8, mid, z + 8, x + px, py, z + pz, x + cx, cy, z + cz);
						 px = cx;
						 py = cy;
						 pz = cz;
					}
				}
				colorIndex = (colorIndex + 1) % colors.length;
			}
			writer.write("]);");
		}
		finally {
			if(writer != null) {
				writer.close();
			}
		}	
	}

	@Override
	public void run() {
		try {
			exportRegionOverlay();
		}
		catch(Exception e) {
			plugin.getServer().getLogger().log(Level.WARNING, "Error writing region.js");
		}
	}
}
