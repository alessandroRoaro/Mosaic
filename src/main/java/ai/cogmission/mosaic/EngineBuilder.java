package ai.cogmission.mosaic;

public interface EngineBuilder<T> {
	/**
	 * Returns an implementation of {@link MosaicEngine}
	 * 
	 * @return	an implementation of {@link MosaicEngine}
	 */
	MosaicEngine<T> build(MosaicPane mosaicPane);
}
