package io.coti.cotinode.model.Interfaces;

import io.coti.cotinode.data.Hash;

import java.io.Serializable;

public interface IEntity extends Serializable {
    Hash getKey();
}
