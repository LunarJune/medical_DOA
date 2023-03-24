package xly.doip;

/**
 * Constants useful in processing DOIP messages.
 */
public class DoipConstants {

    public static final String STATUS_OK = "Status.001";
    public static final String STATUS_BAD_REQUEST = "Status.101";
    public static final String STATUS_UNAUTHENTICATED = "Status.102";
    public static final String STATUS_FORBIDDEN = "Status.103";
    public static final String STATUS_NOT_FOUND = "Status.104";
    public static final String STATUS_CONFLICT = "Status.105";
    public static final String STATUS_DECLINED = "Status.200";
    public static final String STATUS_ERROR = "Status.500";

    public static final String OP_HELLO = "Op.Hello";
    public static final String OP_LIST_OPERATIONS = "Op.ListOperations";
    public static final String OP_CREATE = "Op.Create";
    public static final String OP_RETRIEVE = "Op.Retrieve";
    public static final String OP_UPDATE = "Op.Update";
    public static final String OP_DELETE = "Op.Delete";
    public static final String OP_SEARCH = "Op.Search";
    public static final String OP_GETLHS = "Op.GetLHS";

    public static final String MESSAGE_ATT = "message";
}
