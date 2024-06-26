package com.basma.meal_details.data.repo

import android.util.Log
import com.basma.common.utils.Mapper
import com.basma.common.utils.Resource
import com.basma.meal_details.data.local.MealDetailsLocalDataSourceContract
import com.basma.meal_details.data.local.MealDetailsLocalEntity
import com.basma.meal_details.data.remote.MealDetailsRemoteDataSourceContract
import com.basma.meal_details.domain.contract.MealDetailsRepositoryContract
import com.basma.meal_details.domain.entity.Meal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MealDetailsRepositoryImp @Inject constructor(
    private val localDataSource: MealDetailsLocalDataSourceContract,
    private val remoteDataSource: MealDetailsRemoteDataSourceContract,
    private val mealDetailsDataMapper: Mapper<Meal, MealDetailsLocalEntity>
) : MealDetailsRepositoryContract {

    override suspend fun getMealDetails(mealId: String): Flow<Resource<Meal>> {
        return flow {
            try {
                //Get data from RemoteDataSource
                val response = remoteDataSource.getMealDetails(mealId)
                Log.i("response", response.toString())
                emit(Resource.Success(response.meals[0]))
                //Save to local
                localDataSource.insertMealDetails(mealDetailsDataMapper.from(response.meals[0]))

            } catch (ex: Exception) {
                ex.printStackTrace()
                // If remote request fails, get data from LocalDataSource
                try {
                    val localData = localDataSource.getMealDetailsFromDataBase(mealId)
                    emit(Resource.Success(mealDetailsDataMapper.to(localData)))
                } catch (ex1: Exception) {
                    // Emit error
                    emit(Resource.Error(ex1))
                }
            }
        }
    }
}