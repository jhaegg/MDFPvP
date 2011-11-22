package se.shard.kaustic.mdfpvp.persisting;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.bukkit.block.Block;

/**
 * Class for storing location of player death chests.
 * @author Johan Hägg
 */
@Entity
@Table(name = "MDFPvP_DeathChest")
public class DeathChest {
	@Id
	private int id;
	private int posX;
	private int posY;
	private int posZ;
	private UUID worldUUID;
	@OneToOne
	private PlayerData owner;
	
	public DeathChest() {}
	public DeathChest(Block chest) {
		this.posX = chest.getX();
		this.posY = chest.getY();
		this.posZ = chest.getZ();
		this.worldUUID = chest.getWorld().getUID();
	}
	
	/**
	 * Gets the primary key of the death chest.
	 * @return the primary key of the death chest.
	 */
	public int getId() {		
		return id;
	}
	
	/**
	 * Sets the primary key of the death chest.
	 * @param id the new primary key of the death chest.
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Gets the world x coordinate of the death chest.
	 * @return the world x coordinate of the death chest.
	 */
	public int getPosX() {
		return posX;
	}
	
	/**
	 * Sets the world x coordinate of the death chest.
	 * @param posX the new world x coordinate of the death chest.
	 */
	public void setPosX(int posX) {
		this.posX = posX;
	}
	
	/**
	 * Gets the world y coordinate of the death chest.
	 * @return the world y coordinate of the death chest.
	 */
	public int getPosY() {
		return posY;
	}
	
	/**
	 * Sets the world y coordinate of the death chest.
	 * @param posY the new world y coordinate of the death chest.
	 */
	public void setPosY(int posY) {
		this.posY = posY;
	}
	
	/**
	 * Gets the world z coordinate of the death chest.
	 * @return the world z coordinate of the death chest.
	 */
	public int getPosZ() {
		return posZ;
	}
	
	/**
	 * Sets the world z coordinate of the death chest.
	 * @param posZ the new world z coordinate of the death chest.
	 */
	public void setPosZ(int posZ) {
		this.posZ = posZ;
	}
	
	/**
	 * Gets the bukkit persisting unique id of the world in which the death chest resides.
	 * @return the bukkit persisting unique id of the world in which the death chest resides.
	 */
	public UUID getWorldUUID() {
		return worldUUID;
	}
	
	/**
	 * Sets the bukkit persisting unique id of the world in which the death chest resides.
	 * @param worldUUID the new bukkit persisting unique id of the world in which the death chest resides.
	 */
	public void setWorldUUID(UUID worldUUID) {
		this.worldUUID = worldUUID;
	}
	
	/**
	 * Gets the owner of the death chest.
	 * @return the owner of the death chest.
	 */
	public PlayerData getOwner() {
		return owner;
	}
	
	/**
	 * Sets the owner of the death chest.
	 * @param owner the new owner of the death chest.
	 */
	public void setOwner(PlayerData owner) {
		this.owner = owner;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Block) {
			Block block = (Block)obj;
			return this.getWorldUUID().equals(block.getWorld().getUID()) && this.getPosX() == block.getX() && this.getPosY() == block.getY() && this.getPosZ() == block.getZ(); 
		}
		else if(obj instanceof DeathChest) {
			DeathChest deathChest = (DeathChest)obj;
			return this.getWorldUUID().equals(deathChest.getWorldUUID()) && this.getPosX() == deathChest.getPosX() && this.getPosY() == deathChest.getPosY() && this.getPosZ() == deathChest.getPosZ();
		}
		return false;
	}
}
