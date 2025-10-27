/*
 * Copyright (C) 2020 Indiana University and 2023 University of Nebraska
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package vtea.partition;

/**
 * Strategies for partitioning volumes into chunks
 *
 * @author sethwinfree
 */
public enum PartitioningStrategy {
    /**
     * Fixed chunk size specified by user
     */
    FIXED_SIZE,

    /**
     * Adaptive chunk size based on volume dimensions
     */
    ADAPTIVE,

    /**
     * Memory-based chunk size calculated from available RAM
     */
    MEMORY_BASED
}
