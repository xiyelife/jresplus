package com.hundsun.jresplus.web.velocity.cache;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 */
public interface CompactCache<K, V> {
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void put(K key, V value);

	/**
	 * 
	 * @param key
	 */
	public V get(K key);

	/**
	 * 
	 */
	public void clean();
}
