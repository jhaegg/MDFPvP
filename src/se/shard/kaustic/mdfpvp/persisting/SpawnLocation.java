package se.shard.kaustic.mdfpvp.persisting;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.bukkit.Location;

/**
 * Class for storing player spawn location workaround data
 * @author Johan Hägg
 */
@Entity
@Table(name = "MDFPvP_SpawnLocation")
public class SpawnLocation {
	@Id
	private int id;
	private int posX;
	private int posY;
	private int posZ;
	private UUID worldUUID;
	@OneToOne
	private PlayerData owner;
	
	public SpawnLocation() {}
	
	public SpawnLocation(Location location) {
		posX = location.getBlockX();
		posY = location.getBlockY();
		posZ = location.getBlockZ();
		worldUUID = location.getWorld().getUID();
	}

	/**
	 * Gets the primary key of the spawn location.
	 * @return the primary key of the spawn location.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the primary key of the spawn location.
	 * @param id the new primary key of the spawn location.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the world x position of the spawn location.
	 * @return the world x position of the spawn location.
	 */
	public int getPosX() {
		return posX;
	}

	/**
	 * Sets the world x position of the spawn location.
	 * @param posX the new world x position of the spawn location.
	 */
	public void setPosX(int posX) {
		this.posX = posX;
	}

	/**
	 * Gets the world y position of the spawn location.
	 * @return the world y position of the spawn location.
	 */
	public int getPosY() {
		return posY;
	}

	/**
	 * Sets the world y position of the spawn location.
	 * @param posY the new world y position of the spawn location.
	 */
	public void setPosY(int posY) {
		this.posY = posY;
	}

	/**
	 * Gets the world z position of the spawn location.
	 * @return the world z position of the spawn location.
	 */
	public int getPosZ() {
		return posZ;
	}

	/**
	 * Sets the world z position of the spawn location.
	 * @param posZ the new world z position of the spawn location.
	 */
	public void setPosZ(int posZ) {
		this.posZ = posZ;
	}
	
	/**
	 * Gets the bukkit world unique id of the spawn location.
	 * @return the worldUUID
	 */
	public UUID getWorldUUID() {
		return worldUUID;
	}

	/**
	 * Sets the bukkit world unique id of the spawn location.
	 * @param worldUUID the new bukkit world unique id of the spawn location.
	 */
	public void setWorldUUID(UUID worldUUID) {
		this.worldUUID = worldUUID;
	}

	/**
	 * Gets the owner of the spawn location.
	 * @return the owner of the spawn location.
	 */
	public PlayerData getOwner() {
		return owner;
	}

	/**
	 * Sets the owner of the spawn location.
	 * @param owner the new owner of the spawn location.
	 */
	public void setOwner(PlayerData owner) {
		this.owner = owner;
	}
}
