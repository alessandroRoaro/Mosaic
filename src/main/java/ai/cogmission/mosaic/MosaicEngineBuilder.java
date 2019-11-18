package ai.cogmission.mosaic;


import ai.cogmission.mosaic.refimpl.javafx.MosaicPane;

public class MosaicEngineBuilder<T> implements EngineBuilder<T> {
	/**
	 * Returns a new {@link MosaicEngine}
	 */
	public MosaicEngine<T> build (MosaicPane mosaicPane) {
		return new MosaicEngineImpl<T>(mosaicPane);
	}
}
