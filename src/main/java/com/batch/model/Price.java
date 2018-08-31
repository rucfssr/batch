package com.batch.model;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

/***
 * Model class for price.
 * 
 * @author faizanhussain
 *
 */
public class Price {

	@NotNull
	private long id;

	@NotNull
	private LocalDateTime asOf;

	@NotNull
	private String payload;

	public Price(long id, LocalDateTime asOf, String payload) {
		this.id = id;
		this.asOf = asOf;
		this.payload = payload;
	}

	public long getId() {
		return id;
	}

	public LocalDateTime getAsOf() {
		return asOf;
	}

	public String getPayload() {
		return payload;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((asOf == null) ? 0 : asOf.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Price other = (Price) obj;
		if (asOf == null) {
			if (other.asOf != null)
				return false;
		} else if (!asOf.equals(other.asOf))
			return false;
		if (id != other.id)
			return false;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} else if (!payload.equals(other.payload))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Price [id=" + id + ", asOf=" + asOf + ", payload=" + payload + "]";
	}

}
