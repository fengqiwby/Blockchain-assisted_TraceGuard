package org.fisco.bcos.asset.contract;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.DynamicBytes;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class ShaTest extends Contract {
    public static final String[] BINARY_ARRAY = {"60806040526040805190810160405280600e81526020017f48656c6c6f2c20536861546573740000000000000000000000000000000000008152506000908051906020019061004f9291906100a5565b5034801561005c57600080fd5b50615006600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555061014a565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100e657805160ff1916838001178555610114565b82800160010185558215610114579182015b828111156101135782518255916020019190600101906100f8565b5b5090506101219190610125565b5090565b61014791905b8082111561014357600081600090555060010161012b565b5090565b90565b610746806101596000396000f30060806040526004361061006d576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680632d0ccea6146100725780633bc5de30146100af5780638b053758146100da57806393730bbe14610117578063f246d34614610154575b600080fd5b34801561007e57600080fd5b506100996004803603610094919081019061057e565b610191565b6040516100a69190610604565b60405180910390f35b3480156100bb57600080fd5b506100c4610263565b6040516100d1919061061f565b60405180910390f35b3480156100e657600080fd5b5061010160048036036100fc919081019061057e565b610305565b60405161010e9190610604565b60405180910390f35b34801561012357600080fd5b5061013e6004803603610139919081019061057e565b6103ad565b60405161014b9190610604565b60405180910390f35b34801561016057600080fd5b5061017b6004803603610176919081019061057e565b610419565b6040516101889190610604565b60405180910390f35b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663eb90f459836040518263ffffffff167c010000000000000000000000000000000000000000000000000000000002815260040161020a919061061f565b602060405180830381600087803b15801561022457600080fd5b505af1158015610238573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061025c9190810190610555565b9050919050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102fb5780601f106102d0576101008083540402835291602001916102fb565b820191906000526020600020905b8154815290600101906020018083116102de57829003601f168201915b5050505050905090565b60006002826040518082805190602001908083835b60208310151561033f578051825260208201915060208101905060208303925061031a565b6001836020036101000a0380198251168184511680821785525050505050509050019150506020604051808303816000865af1158015610383573d6000803e3d6000fd5b5050506040513d601f19601f820116820180604052506103a69190810190610555565b9050919050565b6000816040518082805190602001908083835b6020831015156103e557805182526020820191506020810190506020830392506103c0565b6001836020036101000a03801982511681845116808217855250505050505090500191505060405180910390209050919050565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663fb34363c836040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401610492919061061f565b602060405180830381600087803b1580156104ac57600080fd5b505af11580156104c0573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052506104e49190810190610555565b9050919050565b60006104f782516106af565b905092915050565b600082601f830112151561051257600080fd5b81356105256105208261066e565b610641565b9150808252602083016020830185838301111561054157600080fd5b61054c8382846106b9565b50505092915050565b60006020828403121561056757600080fd5b6000610575848285016104eb565b91505092915050565b60006020828403121561059057600080fd5b600082013567ffffffffffffffff8111156105aa57600080fd5b6105b6848285016104ff565b91505092915050565b6105c8816106a5565b82525050565b60006105d98261069a565b8084526105ed8160208601602086016106c8565b6105f6816106fb565b602085010191505092915050565b600060208201905061061960008301846105bf565b92915050565b6000602082019050818103600083015261063981846105ce565b905092915050565b6000604051905081810181811067ffffffffffffffff8211171561066457600080fd5b8060405250919050565b600067ffffffffffffffff82111561068557600080fd5b601f19601f8301169050602081019050919050565b600081519050919050565b6000819050919050565b6000819050919050565b82818337600083830152505050565b60005b838110156106e65780820151818401526020810190506106cb565b838111156106f5576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a723058203eeaeeaaed9dcccda356de1fb9dc3d3458b289b4902b1552db69284e04bf6ec26c6578706572696d656e74616cf50037"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"60806040526040805190810160405280600e81526020017f48656c6c6f2c20536861546573740000000000000000000000000000000000008152506000908051906020019061004f9291906100a5565b5034801561005c57600080fd5b50615006600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555061014a565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100e657805160ff1916838001178555610114565b82800160010185558215610114579182015b828111156101135782518255916020019190600101906100f8565b5b5090506101219190610125565b5090565b61014791905b8082111561014357600081600090555060010161012b565b5090565b90565b610746806101596000396000f30060806040526004361061006d576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634cc1e5cc14610072578063a91dffb5146100af578063a935d284146100ec578063d81443b414610129578063e211e0c114610166575b600080fd5b34801561007e57600080fd5b506100996004803603610094919081019061057e565b610191565b6040516100a69190610604565b60405180910390f35b3480156100bb57600080fd5b506100d660048036036100d1919081019061057e565b610263565b6040516100e39190610604565b60405180910390f35b3480156100f857600080fd5b50610113600480360361010e919081019061057e565b610335565b6040516101209190610604565b60405180910390f35b34801561013557600080fd5b50610150600480360361014b919081019061057e565b6103dd565b60405161015d9190610604565b60405180910390f35b34801561017257600080fd5b5061017b610449565b604051610188919061061f565b60405180910390f35b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663f25611b5836040518263ffffffff167c010000000000000000000000000000000000000000000000000000000002815260040161020a919061061f565b602060405180830381600087803b15801561022457600080fd5b505af1158015610238573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061025c9190810190610555565b9050919050565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663b6510107836040518263ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016102dc919061061f565b602060405180830381600087803b1580156102f657600080fd5b505af115801561030a573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061032e9190810190610555565b9050919050565b60006002826040518082805190602001908083835b60208310151561036f578051825260208201915060208101905060208303925061034a565b6001836020036101000a0380198251168184511680821785525050505050509050019150506020604051808303816000865af11580156103b3573d6000803e3d6000fd5b5050506040513d601f19601f820116820180604052506103d69190810190610555565b9050919050565b6000816040518082805190602001908083835b60208310151561041557805182526020820191506020810190506020830392506103f0565b6001836020036101000a03801982511681845116808217855250505050505090500191505060405180910390209050919050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156104e15780601f106104b6576101008083540402835291602001916104e1565b820191906000526020600020905b8154815290600101906020018083116104c457829003601f168201915b5050505050905090565b60006104f782516106af565b905092915050565b600082601f830112151561051257600080fd5b81356105256105208261066e565b610641565b9150808252602083016020830185838301111561054157600080fd5b61054c8382846106b9565b50505092915050565b60006020828403121561056757600080fd5b6000610575848285016104eb565b91505092915050565b60006020828403121561059057600080fd5b600082013567ffffffffffffffff8111156105aa57600080fd5b6105b6848285016104ff565b91505092915050565b6105c8816106a5565b82525050565b60006105d98261069a565b8084526105ed8160208601602086016106c8565b6105f6816106fb565b602085010191505092915050565b600060208201905061061960008301846105bf565b92915050565b6000602082019050818103600083015261063981846105ce565b905092915050565b6000604051905081810181811067ffffffffffffffff8211171561066457600080fd5b8060405250919050565b600067ffffffffffffffff82111561068557600080fd5b601f19601f8301169050602081019050919050565b600081519050919050565b6000819050919050565b6000819050919050565b82818337600083830152505050565b60005b838110156106e65780820151818401526020810190506106cb565b838111156106f5576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a723058205c12ffa29e4484e1aee135ed6b7f0eb5c02dc354a04b5de2aef2720863db496a6c6578706572696d656e74616cf50037"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":false,\"inputs\":[{\"name\":\"_memory\",\"type\":\"bytes\"}],\"name\":\"calculateKeccak256\",\"outputs\":[{\"name\":\"result\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getData\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_memory\",\"type\":\"bytes\"}],\"name\":\"getSha256\",\"outputs\":[{\"name\":\"result\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_memory\",\"type\":\"bytes\"}],\"name\":\"getKeccak256\",\"outputs\":[{\"name\":\"result\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_memory\",\"type\":\"bytes\"}],\"name\":\"calculateSM3\",\"outputs\":[{\"name\":\"result\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_CALCULATEKECCAK256 = "calculateKeccak256";

    public static final String FUNC_GETDATA = "getData";

    public static final String FUNC_GETSHA256 = "getSha256";

    public static final String FUNC_GETKECCAK256 = "getKeccak256";

    public static final String FUNC_CALCULATESM3 = "calculateSM3";

    protected ShaTest(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public TransactionReceipt calculateKeccak256(byte[] _memory) {
        final Function function = new Function(
                FUNC_CALCULATEKECCAK256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] calculateKeccak256(byte[] _memory, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_CALCULATEKECCAK256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCalculateKeccak256(byte[] _memory) {
        final Function function = new Function(
                FUNC_CALCULATEKECCAK256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<byte[]> getCalculateKeccak256Input(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_CALCULATEKECCAK256, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public Tuple1<byte[]> getCalculateKeccak256Output(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_CALCULATEKECCAK256, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public byte[] getData() throws ContractException {
        final Function function = new Function(FUNC_GETDATA, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeCallWithSingleValueReturn(function, byte[].class);
    }

    public TransactionReceipt getSha256(byte[] _memory) {
        final Function function = new Function(
                FUNC_GETSHA256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] getSha256(byte[] _memory, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_GETSHA256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForGetSha256(byte[] _memory) {
        final Function function = new Function(
                FUNC_GETSHA256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<byte[]> getGetSha256Input(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_GETSHA256, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public Tuple1<byte[]> getGetSha256Output(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_GETSHA256, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public TransactionReceipt getKeccak256(byte[] _memory) {
        final Function function = new Function(
                FUNC_GETKECCAK256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] getKeccak256(byte[] _memory, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_GETKECCAK256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForGetKeccak256(byte[] _memory) {
        final Function function = new Function(
                FUNC_GETKECCAK256, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<byte[]> getGetKeccak256Input(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_GETKECCAK256, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public Tuple1<byte[]> getGetKeccak256Output(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_GETKECCAK256, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public TransactionReceipt calculateSM3(byte[] _memory) {
        final Function function = new Function(
                FUNC_CALCULATESM3, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] calculateSM3(byte[] _memory, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_CALCULATESM3, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCalculateSM3(byte[] _memory) {
        final Function function = new Function(
                FUNC_CALCULATESM3, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(_memory)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<byte[]> getCalculateSM3Input(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_CALCULATESM3, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public Tuple1<byte[]> getCalculateSM3Output(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_CALCULATESM3, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public static ShaTest load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new ShaTest(contractAddress, client, credential);
    }

    public static ShaTest deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(ShaTest.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }
}
