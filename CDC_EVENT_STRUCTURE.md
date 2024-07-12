#### Schema Structure
- `schema`: describes the structure and types of the data.
    - `type`: `struct` indicates the schema is a structured object.
    - `fields`: an array of field definitions within the schema.
#### Fields Explanation
1. `before`: represents the state of the row before the change.
2. `after`: represents the state of the row after the change.
3. `source`: metadata about the source of the change.
4. `op`: represents the type of operation (`c` for create, `u` for update, `d` for delete).
5. `ts_ms`: the timestamp of the operation.
6. `transaction`: information about the transaction if the change is part of one.
#### Payload Structure
`payload`: contains the actual data captured by the CDC event.
- `before`: the state of the row before the change.
- `after`: the state of the row after the change.
- `source`: metadata about the source of the change.
- `op`: the type of operation.
- `ts_ms`: the timestamp of the operation.
- `transaction`: transaction information, if applicable