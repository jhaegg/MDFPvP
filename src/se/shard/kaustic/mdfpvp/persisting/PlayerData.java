package se.shard.kaustic.mdfpvp.persisting;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.bukkit.entity.Player;

import com.avaje.ebean.validation.NotNull;

/**
 * Storage class for data associated with players.
 * @author Johan Hägg
 */
@Entity
@Table(name = "MDFPvP_PlayerData")
public class PlayerData {
	@Id
	private int playerId;
	@NotNull
	private String playerName;
	private UUID playerUUID;
	private int deaths;
	private int kills;
	private int killed;
	@OneToMany(mappedBy = "owner")
	List<Claim> claims;
	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name = "MDFPvP_Tenant",
			joinColumns={@JoinColumn(name = "tenant_id", referencedColumnName = "player_id")})
	List<PlayerData> tenants;
	
	public PlayerData() {}
	
	public PlayerData(Player player) {
		playerName = player.getName();
		playerUUID = player.getUniqueId();
		deaths = 0;
		kills = 0;
		killed = 0;
	}
	
	/**
	 * Adds a new claim to the players list of claims. Updates the owner of the claim to reflect this.
	 * @param claim the claim which should be added to the list of claims.
	 */
	public void addClaim(Claim claim) {
		if(!claims.contains(claim)) {
			claims.add(claim);
			claim.setOwner(this);
		}
	}
	
	public boolean addTenant(PlayerData tenant) {
		if(!tenants.contains(tenant)) {
			tenants.add(tenant);
			return true;
		}
		return false;
	}
	
	public boolean removeTenant(PlayerData tenant) {
		if(tenants.contains(tenant)) {
			tenants.remove(tenant);
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the primary key of the player data.
	 * @return the player primary key.
	 */
	public int getPlayerId() {
		return playerId;
	}

	/**
	 * Sets the primary key of the player data.
	 * @param playerId the new primary key.
	 */
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	/**
	 * Gets the name of the player.
	 * @return the name of the player.
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * Sets the name of the player.
	 * @param playerName the new player name.
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * Gets the persisting bukkit unique id of the player.
	 * @return the persisting bukkit unique id of the player.
	 */
	public UUID getPlayerUUID() {
		return playerUUID;
	}

	/**
	 * Sets the persisting bukkit unique id of the player.
	 * @param playerUUID the new persisting bukkit unique id of the player.
	 */
	public void setPlayerUUID(UUID playerUUID) {
		this.playerUUID = playerUUID;
	}

	/**
	 * Gets the number of times the player has died.
	 * @return the number of times the player has died.
	 */
	public int getDeaths() {
		return deaths;
	}

	/**
	 * Sets the number of times the player has died.
	 * @param deaths the new number of times the player has died.
	 */
	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	/**
	 * Gets the number of times the player has killed another player.
	 * @return the number of times the player has killed another player.
	 */
	public int getKills() {
		return kills;
	}

	/**
	 * Sets the number of times the player has killed another player.
	 * @param kills new the number of times the player has killed another player.
	 */
	public void setKills(int kills) {
		this.kills = kills;
	}

	/**
	 * Gets the number of times the player has been killed by another player.
	 * @return the number of times the player has been killed by another player.
	 */
	public int getKilled() {
		return killed;
	}

	/**
	 * Sets the number of times the player has been killed by another player.
	 * @param killed the new number of times the player has been killed by another player.
	 */
	public void setKilled(int killed) {
		this.killed = killed;
	}

	/**
	 * Gets the collection of claims owned by the player.
	 * @return the collections of claims owned by the player.
	 */
	public List<Claim> getClaims() {
		return claims;
	}

	/**
	 * Sets the collection of claims owned by the player.
	 * @param claims the new collection of claims owned by the player.
	 */
	public void setClaims(List<Claim> claims) {
		this.claims = claims;
	}

	/**
	 * Gets the list of players allowed to change the claims owned by this player.
	 * @return the list of tenants allowed to change the claims owned by this player. 
	 */
	public List<PlayerData> getTenants() {
		return tenants;
	}

	/**
	 * Sets the list of players allowed to change the claims owned by this player.
	 * @param tenants the new list of tenants allowed to change the claims owned by this player.
	 */
	public void setTenants(List<PlayerData> tenants) {
		this.tenants = tenants;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PlayerData) { 
			return ((PlayerData)obj).playerUUID.equals(this.playerUUID);
		}
		return false;
	}
	
	
}
