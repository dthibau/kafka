/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package org.formation.model;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Courier extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -4921226110039771984L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Courier\",\"namespace\":\"org.formation.model\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"vehicle_id\",\"type\":\"int\"},{\"name\":\"first_name\",\"type\":\"string\",\"default\":\"undefined\"},{\"name\":\"position\",\"type\":[{\"type\":\"record\",\"name\":\"Position\",\"fields\":[{\"name\":\"latitude\",\"type\":\"double\"},{\"name\":\"longitude\",\"type\":\"double\"}]}]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<Courier> ENCODER =
      new BinaryMessageEncoder<Courier>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<Courier> DECODER =
      new BinaryMessageDecoder<Courier>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<Courier> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<Courier> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<Courier>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this Courier to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a Courier from a ByteBuffer. */
  public static Courier fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  @Deprecated public java.lang.CharSequence id;
  @Deprecated public int vehicle_id;
  @Deprecated public java.lang.CharSequence first_name;
  @Deprecated public java.lang.Object position;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public Courier() {}

  /**
   * All-args constructor.
   * @param id The new value for id
   * @param vehicle_id The new value for vehicle_id
   * @param first_name The new value for first_name
   * @param position The new value for position
   */
  public Courier(java.lang.CharSequence id, java.lang.Integer vehicle_id, java.lang.CharSequence first_name, java.lang.Object position) {
    this.id = id;
    this.vehicle_id = vehicle_id;
    this.first_name = first_name;
    this.position = position;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return vehicle_id;
    case 2: return first_name;
    case 3: return position;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.CharSequence)value$; break;
    case 1: vehicle_id = (java.lang.Integer)value$; break;
    case 2: first_name = (java.lang.CharSequence)value$; break;
    case 3: position = (java.lang.Object)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'id' field.
   * @return The value of the 'id' field.
   */
  public java.lang.CharSequence getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.CharSequence value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'vehicle_id' field.
   * @return The value of the 'vehicle_id' field.
   */
  public java.lang.Integer getVehicleId() {
    return vehicle_id;
  }

  /**
   * Sets the value of the 'vehicle_id' field.
   * @param value the value to set.
   */
  public void setVehicleId(java.lang.Integer value) {
    this.vehicle_id = value;
  }

  /**
   * Gets the value of the 'first_name' field.
   * @return The value of the 'first_name' field.
   */
  public java.lang.CharSequence getFirstName() {
    return first_name;
  }

  /**
   * Sets the value of the 'first_name' field.
   * @param value the value to set.
   */
  public void setFirstName(java.lang.CharSequence value) {
    this.first_name = value;
  }

  /**
   * Gets the value of the 'position' field.
   * @return The value of the 'position' field.
   */
  public java.lang.Object getPosition() {
    return position;
  }

  /**
   * Sets the value of the 'position' field.
   * @param value the value to set.
   */
  public void setPosition(java.lang.Object value) {
    this.position = value;
  }

  /**
   * Creates a new Courier RecordBuilder.
   * @return A new Courier RecordBuilder
   */
  public static org.formation.model.Courier.Builder newBuilder() {
    return new org.formation.model.Courier.Builder();
  }

  /**
   * Creates a new Courier RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new Courier RecordBuilder
   */
  public static org.formation.model.Courier.Builder newBuilder(org.formation.model.Courier.Builder other) {
    return new org.formation.model.Courier.Builder(other);
  }

  /**
   * Creates a new Courier RecordBuilder by copying an existing Courier instance.
   * @param other The existing instance to copy.
   * @return A new Courier RecordBuilder
   */
  public static org.formation.model.Courier.Builder newBuilder(org.formation.model.Courier other) {
    return new org.formation.model.Courier.Builder(other);
  }

  /**
   * RecordBuilder for Courier instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Courier>
    implements org.apache.avro.data.RecordBuilder<Courier> {

    private java.lang.CharSequence id;
    private int vehicle_id;
    private java.lang.CharSequence first_name;
    private java.lang.Object position;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(org.formation.model.Courier.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.vehicle_id)) {
        this.vehicle_id = data().deepCopy(fields()[1].schema(), other.vehicle_id);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.first_name)) {
        this.first_name = data().deepCopy(fields()[2].schema(), other.first_name);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.position)) {
        this.position = data().deepCopy(fields()[3].schema(), other.position);
        fieldSetFlags()[3] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing Courier instance
     * @param other The existing instance to copy.
     */
    private Builder(org.formation.model.Courier other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.vehicle_id)) {
        this.vehicle_id = data().deepCopy(fields()[1].schema(), other.vehicle_id);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.first_name)) {
        this.first_name = data().deepCopy(fields()[2].schema(), other.first_name);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.position)) {
        this.position = data().deepCopy(fields()[3].schema(), other.position);
        fieldSetFlags()[3] = true;
      }
    }

    /**
      * Gets the value of the 'id' field.
      * @return The value.
      */
    public java.lang.CharSequence getId() {
      return id;
    }

    /**
      * Sets the value of the 'id' field.
      * @param value The value of 'id'.
      * @return This builder.
      */
    public org.formation.model.Courier.Builder setId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'id' field has been set.
      * @return True if the 'id' field has been set, false otherwise.
      */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'id' field.
      * @return This builder.
      */
    public org.formation.model.Courier.Builder clearId() {
      id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'vehicle_id' field.
      * @return The value.
      */
    public java.lang.Integer getVehicleId() {
      return vehicle_id;
    }

    /**
      * Sets the value of the 'vehicle_id' field.
      * @param value The value of 'vehicle_id'.
      * @return This builder.
      */
    public org.formation.model.Courier.Builder setVehicleId(int value) {
      validate(fields()[1], value);
      this.vehicle_id = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'vehicle_id' field has been set.
      * @return True if the 'vehicle_id' field has been set, false otherwise.
      */
    public boolean hasVehicleId() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'vehicle_id' field.
      * @return This builder.
      */
    public org.formation.model.Courier.Builder clearVehicleId() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'first_name' field.
      * @return The value.
      */
    public java.lang.CharSequence getFirstName() {
      return first_name;
    }

    /**
      * Sets the value of the 'first_name' field.
      * @param value The value of 'first_name'.
      * @return This builder.
      */
    public org.formation.model.Courier.Builder setFirstName(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.first_name = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'first_name' field has been set.
      * @return True if the 'first_name' field has been set, false otherwise.
      */
    public boolean hasFirstName() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'first_name' field.
      * @return This builder.
      */
    public org.formation.model.Courier.Builder clearFirstName() {
      first_name = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'position' field.
      * @return The value.
      */
    public java.lang.Object getPosition() {
      return position;
    }

    /**
      * Sets the value of the 'position' field.
      * @param value The value of 'position'.
      * @return This builder.
      */
    public org.formation.model.Courier.Builder setPosition(java.lang.Object value) {
      validate(fields()[3], value);
      this.position = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'position' field has been set.
      * @return True if the 'position' field has been set, false otherwise.
      */
    public boolean hasPosition() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'position' field.
      * @return This builder.
      */
    public org.formation.model.Courier.Builder clearPosition() {
      position = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Courier build() {
      try {
        Courier record = new Courier();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.vehicle_id = fieldSetFlags()[1] ? this.vehicle_id : (java.lang.Integer) defaultValue(fields()[1]);
        record.first_name = fieldSetFlags()[2] ? this.first_name : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.position = fieldSetFlags()[3] ? this.position : (java.lang.Object) defaultValue(fields()[3]);
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<Courier>
    WRITER$ = (org.apache.avro.io.DatumWriter<Courier>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<Courier>
    READER$ = (org.apache.avro.io.DatumReader<Courier>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}