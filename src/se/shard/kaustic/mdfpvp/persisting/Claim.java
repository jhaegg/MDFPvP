package se.shard.kaustic.mdfpvp.persisting;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.bukkit.Chunk;

@Entity
@Table(name = "MDFPvP_Claim")
public class Claim {
	@Id
	private int claimId;
	private int chunkX;
	private int chunkZ;
	private UUID worldUUID;
	@ManyToOne
	private PlayerData owner;
	private boolean protect;

	public Claim() {}
	
	public Claim(Chunk chunk) {
		chunkX = chunk.getX();
		chunkZ = chunk.getZ();
		worldUUID = chunk.getWorld().getUID();
	}

	/**
	 * Gets the primary key of the claim.
	 * @return the primary key of the claim.
	 */
	public int getClaimId() {
		return claimId;
	}

	/**
	 * Sets the primary key of the claim.
	 * @param id the new primary key of the claim.
	 */
	public void setClaimId(int claimId) {
		this.claimId = claimId;
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
	}

	/**
	 * Gets if the claims is protected from creepers.
	 * @return true if the chunk is protected, otherwise false.
	 */
	public boolean isProtect() {
		return protect;
	}

	/**
	 * Sets if the claim is protected from creepers.
	 * @param protect true if the claim should be protected from creepers, otherwise false.
	 */
	public void setProtect(boolean protect) {
		this.protect = protect;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Claim) {
			Claim claim = (Claim)obj;
			return claim.getWorldUUID().equals(this.getWorldUUID()) && claim.getChunkX() == this.getChunkX() && claim.getChunkZ() == this.getChunkZ();
		}		
		return false;
	}
}