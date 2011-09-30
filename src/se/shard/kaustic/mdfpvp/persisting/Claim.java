package se.shard.kaustic.mdfpvp.persisting;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.bukkit.Chunk;

@Entity
@Table(name = "MDFPvP_Claim")
public class Claim {
	@Id
	@Column(name = "ClaimId")
	private int Id;
	private int chunkX;
	private int chunkZ;
	private UUID worldUUID;
	@ManyToOne
	@JoinColumn(name = "OwnerId")
	private PlayerData owner;
	private boolean protect;

	public Claim(Chunk chunk) {
		chunkX = chunk.getX();
		chunkZ = chunk.getZ();
		worldUUID = chunk.getWorld().getUID();
	}

	/**
	 * Gets the primary key of the claim.
	 * @return the primary key of the claim.
	 */
	public int getId() {
		return Id;
	}

	/**
	 * Sets the primary key of the claim.
	 * @param id the new primary key of the claim.
	 */
	public void setId(int id) {
		Id = id;
	}

	/**
	 * Gets the chunk x coordinate of the claim.
	 * @return the chunk x coordinate of the claim.
	 */
	public int getChunkX() {
		return chunkX;
	}

	/**
	 * Sets the chunk x coordinate of the claim.
	 * @param chunkX the new chunk x coordinate of the claim.
	 */
	public void setChunkX(int chunkX) {
		this.chunkX = chunkX;
	}

	/**
	 * Gets the chunk z coordinate of the claim.
	 * @return the chunk z coordinate of the claim.
	 */
	public int getChunkZ() {
		return chunkZ;
	}

	/**
	 * Sets the chunk z coordinate of the claims.
	 * @param chunkZ the new chunk z coordinate of the claim.
	 */
	public void setChunkZ(int chunkZ) {
		this.chunkZ = chunkZ;
	}

	/**
	 * Gets the bukkit persisting world unique id of the claim.
	 * @return the bukkit persisting world unique id of the claim.
	 */
	public UUID getWorldUUID() {
		return worldUUID;
	}

	/**
	 * Sets the bukkit persisting world unique id of the claim.
	 * @param worldUUID the new bukkit persisting world unique id of the claim.
	 */
	public void setWorldUUID(UUID worldUUID) {
		this.worldUUID = worldUUID;
	}

	/**
	 * Gets the owner of the claim.
	 * @return the owner of the claim.
	 */
	public PlayerData getOwner() {
		return owner;
	}

	/**
	 * Sets the owner of the claim and adds the claim to the owners claim collection.
	 * @param owner the owner to set.
	 */
	public void setOwner(PlayerData owner) {
		this.owner = owner;
		if(!owner.getClaims().contains(this)) {
			owner.getClaims().add(this);
		}
	}

	/**
	 * Gets if the claims is protected from creepers.
	 * @return true if the chunk is protected, otherwise false.
	 */
	public boolean getProtect() {
		return protect;
	}

	/**
	 * Sets if the claim is protected from creepers.
	 * @param protect true if the claim should be protected from creepers, otherwise false.
	 */
	public void setProtect(boolean protect) {
		this.protect = protect;
	}	
}