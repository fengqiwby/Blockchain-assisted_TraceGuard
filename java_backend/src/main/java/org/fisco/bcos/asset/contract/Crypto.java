package org.fisco.bcos.asset.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.Bool;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class Crypto extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b506103b4806100206000396000f300608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806352ce50e814610067578063cbdb3a671461017b578063eb90f45914610259578063fb34363c146102de575b600080fd5b34801561007357600080fd5b5061015a600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610363565b60405180831515151581526020018281526020019250505060405180910390f35b34801561018757600080fd5b5061020c6004803603810190808035600019169060200190929190803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091929192908035600019169060200190929190803560001916906020019092919050505061036e565b60405180831515151581526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390f35b34801561026557600080fd5b506102c0600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061037a565b60405180826000191660001916815260200191505060405180910390f35b3480156102ea57600080fd5b50610345600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610381565b60405180826000191660001916815260200191505060405180910390f35b600080935093915050565b60008094509492505050565b6000919050565b60009190505600a165627a7a7230582077a8eaee84a94f2e3c82ccc81d6b9b613577e195904379f22128b228a3fc4cf40029"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b506103b4806100206000396000f300608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063226f66e01461006757806322ede61e1461017b578063b651010714610259578063f25611b5146102de575b600080fd5b34801561007357600080fd5b5061015a600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610363565b60405180831515151581526020018281526020019250505060405180910390f35b34801561018757600080fd5b5061020c6004803603810190808035600019169060200190929190803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091929192908035600019169060200190929190803560001916906020019092919050505061036e565b60405180831515151581526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390f35b34801561026557600080fd5b506102c0600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061037a565b60405180826000191660001916815260200191505060405180910390f35b3480156102ea57600080fd5b50610345600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610381565b60405180826000191660001916815260200191505060405180910390f35b600080935093915050565b60008094509492505050565b6000919050565b60009190505600a165627a7a723058205dc6b6b4ed83e3c5813d78cea8781afdea385382caba7437e60827cc6c9f69f30029"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":true,\"inputs\":[{\"name\":\"input\",\"type\":\"string\"},{\"name\":\"vrfPublicKey\",\"type\":\"string\"},{\"name\":\"vrfProof\",\"type\":\"string\"}],\"name\":\"curve25519VRFVerify\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"message\",\"type\":\"bytes32\"},{\"name\":\"publicKey\",\"type\":\"bytes\"},{\"name\":\"r\",\"type\":\"bytes32\"},{\"name\":\"s\",\"type\":\"bytes32\"}],\"name\":\"sm2Verify\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"},{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"data\",\"type\":\"bytes\"}],\"name\":\"keccak256Hash\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"data\",\"type\":\"bytes\"}],\"name\":\"sm3\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_CURVE25519VRFVERIFY = "curve25519VRFVerify";

    public static final String FUNC_SM2VERIFY = "sm2Verify";

    public static final String FUNC_KECCAK256HASH = "keccak256Hash";

    public static final String FUNC_SM3 = "sm3";

    protected Crypto(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public Tuple2<Boolean, BigInteger> curve25519VRFVerify(String input, String vrfPublicKey, String vrfProof) throws ContractException {
        final Function function = new Function(FUNC_CURVE25519VRFVERIFY, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.Utf8String(input), 
                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(vrfPublicKey), 
                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(vrfProof)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<Boolean, BigInteger>(
                (Boolean) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue());
    }

    public Tuple2<Boolean, String> sm2Verify(byte[] message, byte[] publicKey, byte[] r, byte[] s) throws ContractException {
        final Function function = new Function(FUNC_SM2VERIFY, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32(message), 
                new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(publicKey), 
                new org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32(r), 
                new org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32(s)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Address>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<Boolean, String>(
                (Boolean) results.get(0).getValue(), 
                (String) results.get(1).getValue());
    }

    public byte[] keccak256Hash(byte[] data) throws ContractException {
        final Function function = new Function(FUNC_KECCAK256HASH, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(data)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeCallWithSingleValueReturn(function, byte[].class);
    }

    public byte[] sm3(byte[] data) throws ContractException {
        final Function function = new Function(FUNC_SM3, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(data)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeCallWithSingleValueReturn(function, byte[].class);
    }

    public static Crypto load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new Crypto(contractAddress, client, credential);
    }

    public static Crypto deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(Crypto.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }
}
