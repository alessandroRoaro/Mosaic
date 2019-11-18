package ai.cogmission.mosaic;

import ai.cogmission.mosaic.refimpl.javafx.MosaicPane;

public interface EngineBuilder<T> {
	/**
	 * Returns an implementation of {@link MosaicEngine}
	 * 
	 * @return	an implementation of {@link MosaicEngine}
	 */
	MosaicEngine<T> build(MosaicPane mosaicPane);
}
