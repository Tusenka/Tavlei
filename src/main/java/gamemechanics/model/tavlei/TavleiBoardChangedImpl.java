package gamemechanics.model.tavlei;

/**
 * Created by Irina
 */
@SuppressWarnings("unused")
public class TavleiBoardChangedImpl extends TavleiBoardImpl {
    private final static byte SIZE = 10;

    @Override
    public byte getSize() {
        return SIZE;
    }
}
