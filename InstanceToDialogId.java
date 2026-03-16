import java.time.Instant;
import java.util.UUID;

public final class InstanceToDialogId {
    private InstanceToDialogId() {
    }

    /**
     * Equivalent to C# ToVersion7(Guid guid, DateTimeOffset timestamp).
     * See https://github.com/Altinn/altinn-dialogporten-adapter/blob/1da45336dec55a805a89aa6afdb42f50e1fa91ce/src/Altinn.DialogportenAdapter.WebApi/Common/Extensions/GuidExtensions.cs#L21
     *
     * It keeps the non-v7 payload bits from the input UUID, but overwrites:
     * - the top 48 bits with Unix epoch milliseconds
     * - the version bits with version 7
     * - the variant bits with RFC 4122 variant (10)
     *
     * Bit layout after the rewrite:
     *
     *   msb (64 bits)
     *   63                                                          16 15    12 11           0
     *   +-------------------------------------------------------------+--------+--------------+
     *   |                  unixTimestampMillis (48 bits)              |  0111  | preserved    |
     *   +-------------------------------------------------------------+--------+--------------+
     *
     *   lsb (64 bits)
     *   63 62 61                                                                             0
     *   +--+--+-------------------------------------------------------------------------------+
     *   | 1| 0| preserved                                                                     |
     *   +--+--+-------------------------------------------------------------------------------+
     *
     * The expressions below map directly to those fields:
     *
     *   ((millis & 0xFFFFFFFFFFFFL) << 16)
     *     Put the 48-bit timestamp in msb bits 63..16.
     *
     *   (msb & 0xFFFFL)
     *     Keep the original msb bits 15..0.
     *
     *   (msb & ~(0xFL << 12)) | (0x7L << 12)
     *     Replace msb bits 15..12 with 0111 (UUID version 7).
     *
     *   (lsb & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L
     *     Replace lsb bits 63..62 with 10 (RFC 4122 variant).
     */
    public static UUID convert(UUID instanceId, Instant createdAt) {
        long msb = instanceId.getMostSignificantBits();
        long lsb = instanceId.getLeastSignificantBits();
        long unixTimestampMillis = createdAt.toEpochMilli();

        msb = ((unixTimestampMillis & 0xFFFFFFFFFFFFL) << 16) | (msb & 0xFFFFL);

        // Set version to 7.
        msb = (msb & ~(0xFL << 12)) | (0x7L << 12);

        // Set RFC 4122 variant (10xxxxxx).
        lsb = (lsb & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

        return new UUID(msb, lsb);
    }
}
