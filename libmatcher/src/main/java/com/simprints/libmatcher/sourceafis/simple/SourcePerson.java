package com.simprints.libmatcher.sourceafis.simple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

 /**
  * Collection of {@link SourceFingerprint}s belonging to one person.
  * 
  * This class is primarily a way to group multiple {@link SourceFingerprint}s belonging to one person.
  * This is very convenient feature when there are multiple fingerprints per person, because
  * it is possible to match two {@link SourcePerson}s directly instead of iterating over their {@link SourceFingerprint}s.
  * <p>
  * {@link #setId Id} property is provided as a simple means to bind {@link SourcePerson} objects to application-specific
  * information. If you need more flexibility, inherit from {@link SourcePerson} class and add
  * application-specific fields as necessary.
  * <p>
  * This class is designed to be easy to serialize in order to be stored in binary format (BLOB)
  * in application database, binary or XML files, or sent over network. You can either serialize
  * the whole {@link SourcePerson} or serialize individual {@link SourceFingerprint}s.
  * 
  * @see SourceFingerprint
  * @serial exclude
  */
 
@SuppressWarnings("serial")
public class SourcePerson implements Cloneable,Serializable
{
    private int Id;
    private List<SourceFingerprint> fingerprints = new ArrayList<SourceFingerprint>();

    /**
     * Creates an empty {@code SourcePerson} object.
     */
    public SourcePerson() { }

    /**
     * Creates new {@code SourcePerson} object and initializes it with a list of
     * {@link SourceFingerprint}s.
     * 
     * @param fingerprints
     *            {@link SourceFingerprint} objects to add to the new {@code SourcePerson}
     */
    public SourcePerson(SourceFingerprint... fingerprints) {
    	for(SourceFingerprint fp:fingerprints){
    		this.fingerprints.add(fp);
    	}
        //this.Fingerprints =  fingerprints.ToList();
    }

    /**
     * Gets application-defined ID for the SourcePerson.
     * 
     * See {@link #setId setId} for explanation. This method just returns
     * previously set ID.
     * 
     * @return ID that was previously set via {@link #setId setId}
     * @see #setId setId
     */
    public int getId() {
		return Id;
	}
    
    /**
     * Sets application-defined ID for the {@code SourcePerson}.
     * 
     * SourceAFIS doesn't use this ID. It is provided for applications as an
     * easy means to link {@code SourcePerson} objects back to application-specific
     * data. Applications can store any integer ID in this field, for example
     * database table key or an array index.
     * <p>
     * Applications that need to attach more detailed information to the person
     * should inherit from {@code SourcePerson} class and add fields as necessary.
     * 
     * @param id
     *            arbitrary application-defined ID
     * @see #getId getId
     */
    public void setId(int id) {
        Id = id;
    }
    
    /**
     * Gets list of {@link SourceFingerprint}s belonging to the {@link SourcePerson}.
     * 
     * This collection is initially empty. Add {@link SourceFingerprint} objects
     * to the returned collection.
     * 
     * @see #setFingerprints setFingerprints
     * @see SourceFingerprint
     */
    public List<SourceFingerprint> getFingerprints() {
        return fingerprints;
    }

    /**
     * Sets list of {@link SourceFingerprint}s belonging to the {@link SourcePerson}.
     * 
     * You can assign the whole collection using this method. Individual
     * {@link SourceFingerprint}s can be added to the collection returned from
     * {@link #getFingerprints getFingerprints}.
     * 
     * @param fingerprints
     *            new list of {@link SourceFingerprint}s for this {@code SourcePerson}
     * @see #getFingerprints getFingerprints
     * @see SourceFingerprint
     */
    public void setFingerprints(List<SourceFingerprint> fingerprints) {
        this.fingerprints = fingerprints;
    }

    /**
     * Creates deep copy of the SourcePerson.
     * 
     * This method clones all {@link SourceFingerprint} objects contained in this
     * {@code SourcePerson}.
     * 
     * @return deep copy of the {@code SourcePerson}
     */
    public SourcePerson clone() {
	    SourcePerson copy = new SourcePerson();
	    copy.Id = Id;
	    for (SourceFingerprint fingerprint : fingerprints)
	        copy.fingerprints.add(fingerprint.clone());
		return copy;
    }

    void CheckForNulls()
    {
        for (SourceFingerprint fp : fingerprints)
            if (fp == null)
                throw new RuntimeException("SourcePerson contains null SourceFingerprint references.");
    }
}
