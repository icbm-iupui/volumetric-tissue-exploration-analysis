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

import vtea.dataset.volume.ChunkedVolumeDataset;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator for processing chunks from a ChunkedVolumeDataset.
 * Provides sequential access to volume chunks for processing pipelines.
 *
 * @author sethwinfree
 */
public class ChunkIterator implements Iterator<Chunk> {

    private final ChunkedVolumeDataset dataset;
    private final int channel;
    private final int totalChunks;
    private int currentIndex;

    /**
     * Constructor
     * @param dataset ChunkedVolumeDataset to iterate
     * @param channel channel index
     */
    public ChunkIterator(ChunkedVolumeDataset dataset, int channel) {
        this.dataset = dataset;
        this.channel = channel;
        this.totalChunks = dataset.getTotalChunkCount();
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < totalChunks;
    }

    @Override
    public Chunk next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more chunks available");
        }

        Chunk chunk = dataset.getChunk(currentIndex, channel);
        currentIndex++;
        return chunk;
    }

    /**
     * Get current progress as percentage
     * @return progress (0.0 to 1.0)
     */
    public double getProgress() {
        return (double) currentIndex / totalChunks;
    }

    /**
     * Get current chunk index
     * @return chunk index
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Get total number of chunks
     * @return total chunks
     */
    public int getTotalChunks() {
        return totalChunks;
    }

    /**
     * Reset iterator to beginning
     */
    public void reset() {
        currentIndex = 0;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }
}
