package com.nnt.lazywork.serviceresult

sealed class Result<out T: Any> {
    /**
     * use for initial case
     */
    object Empty : Result<Nothing>()
    class Success <out T: Any>(val data: T?) : Result<T>()
    class Error(val errorModel: ErrorResponse) : Result<Nothing>()
    object Loading : Result<Nothing>()

    override fun toString(): String {
        return if(this is Success<T>){
            data.toString()
        } else if(this is Error) {
            (this).errorModel.toString()
        } else if(this is Loading) {
            "Loading"
        }
        else "Empty"
    }
}