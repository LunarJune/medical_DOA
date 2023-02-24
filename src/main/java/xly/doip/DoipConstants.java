package xly.doip;

/**
 * Constants useful in processing DOIP messages.
 */
public class DoipConstants {

    public static final String STATUS_OK = "0.DOIP/Status.001";
    public static final String STATUS_BAD_REQUEST = "0.DOIP/Status.101";
    public static final String STATUS_UNAUTHENTICATED = "0.DOIP/Status.102";
    public static final String STATUS_FORBIDDEN = "0.DOIP/Status.103";
    public static final String STATUS_NOT_FOUND = "0.DOIP/Status.104";
    public static final String STATUS_CONFLICT = "0.DOIP/Status.105";
    public static final String STATUS_DECLINED = "0.DOIP/Status.200";
    public static final String STATUS_ERROR = "0.DOIP/Status.500";

    public static final String OP_HELLO = "0.DOIP/Op.Hello";
    public static final String OP_LIST_OPERATIONS = "0.DOIP/Op.ListOperations";
    public static final String OP_CREATE = "0.DOIP/Op.Create";
    public static final String OP_RETRIEVE = "0.DOIP/Op.Retrieve";
    public static final String OP_UPDATE = "0.DOIP/Op.Update";
    public static final String OP_DELETE = "0.DOIP/Op.Delete";
    public static final String OP_SEARCH = "0.DOIP/Op.Search";

    public static final String MESSAGE_ATT = "message";
}
